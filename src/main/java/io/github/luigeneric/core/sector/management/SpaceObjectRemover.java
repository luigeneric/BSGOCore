package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.movement.maneuver.DirectionalManeuver;
import io.github.luigeneric.core.player.counters.CounterFacade;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.protocols.game.RespawnOptions;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.SectorJob;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.objleft.*;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.utils.TimestampedCounter;
import io.github.luigeneric.utils.publishersubscriber.Subscriber;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SpaceObjectRemover implements ISpaceObjectRemover, SectorJob
{
    private final Deque<ObjectLeftDescription> objectLeftDescriptions;
    private final SectorContext ctx;
    private final List<ObjectLeftSubscriber> subscribers;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;
    private final Set<Long> userIdsInHoldForRemove;
    private final Deque<Long> userIdsNotifiedForRemove;
    private final Set<Long> userIdsDisconnected;
    private final MicrometerRegistry micrometerRegistry;
    private final TimestampedCounter cylonKilledCounter;
    private final TimestampedCounter colonialKilledCounter;

    public SpaceObjectRemover(final Deque<ObjectLeftDescription> objectLeftDescriptions,
                              final SectorContext ctx,
                              final GameProtocolWriteOnly gameProtocolWriteOnly
    )
    {
        this.ctx = ctx;
        this.objectLeftDescriptions = objectLeftDescriptions;
        this.subscribers = new ArrayList<>();
        this.gameProtocolWriteOnly = gameProtocolWriteOnly;
        this.userIdsDisconnected = new CopyOnWriteArraySet<>();
        this.userIdsInHoldForRemove = new HashSet<>();
        this.userIdsNotifiedForRemove = new ConcurrentLinkedDeque<>();
        this.colonialKilledCounter = new TimestampedCounter(Faction.Colonial);
        this.cylonKilledCounter = new TimestampedCounter(Faction.Cylon);
        this.micrometerRegistry = CDI.current().select(MicrometerRegistry.class).get();
    }

    private void addObjectDied(final SpaceObject obj, final SpaceObject killerObject)
    {
        if (obj.isRemoved())
        {
            log.warn("Object {} is already removed, cause: {}", obj.getObjectID(), obj.getRemovingCauseDirect());
            return;
        }
        this.objectLeftDescriptions.offer(new ObjectLeftDeath(obj, killerObject));
    }

    @Override
    public void notifyRemovingCauseAdded(final SpaceObject spaceObject, final RemovingCause removingCause, final SpaceObject removingCauseObject)
    {
        if (spaceObject.isRemoved())
        {
            log.warn("SpaceObject id={}, type={} is already removed, cause={}, newCause={}",
                    spaceObject.getObjectID(),
                    spaceObject.getSpaceEntityType(),
                    spaceObject.getRemovingCause(),
                    removingCause
            );
            return;
        }
        switch (removingCause)
        {
            case Death ->
            {
                this.addObjectDied(spaceObject, removingCauseObject);
            }
            case JumpOut ->
            {
                this.addObjectJumpOut(spaceObject);
            }
            case Disconnection ->
            {
                this.addObjectDisconnected(spaceObject);
            }
            case Dock ->
            {
                this.addObjectDock(spaceObject);
            }
            case Hit ->
            {
                this.addObjectLeftHit(spaceObject, removingCauseObject);
            }
            default -> throw new IllegalStateException(removingCause + " not implemented yet!");
        }
    }

    @Override
    public void notifyRemovingCauseAdded(final SpaceObject spaceObject, final RemovingCause removingCause)
    {
        this.notifyRemovingCauseAdded(spaceObject, removingCause, null);
    }

    @Override
    public void playerSelectedRespawnLocation(final long userID)
    {
        this.userIdsNotifiedForRemove.offer(userID);
    }

    @Override
    public void notifyUserDisconnected(final long userId)
    {
        userIdsDisconnected.add(userId);
    }

    private void addObjectDock(final SpaceObject obj)
    {
        this.objectLeftDescriptions.offer(new ObjectLeftDock(obj));
    }
    private void addObjectDisconnected(final SpaceObject obj)
    {
        this.objectLeftDescriptions.offer(new ObjectLeftDisconnect(obj));
    }
    private void addObjectJumpOut(final SpaceObject obj)
    {
        if (obj.isRemoved())
        {
            logObjectAlreadyRemoved(obj);
            return;
        }
        this.objectLeftDescriptions.offer(new ObjectLeftJumpOut(obj));
    }

    private void addObjectLeftHit(final SpaceObject obj, final SpaceObject targetHit)
    {
        if (obj.isRemoved())
        {
            logObjectAlreadyRemoved(obj);
            return;
        }
        this.objectLeftDescriptions.offer(new ObjectLeftHit(obj, targetHit));
    }

    @Override
    public void run()
    {
        objectLeftUpdate();

        playerLeftUpdate();

        updateUserDisconnected();
    }

    private void updateUserDisconnected()
    {
        final Set<Long> forRemoval = new HashSet<>();
        for (final long idOfDisconnectedUser : userIdsDisconnected)
        {
            //Only remove users which are actually disconnected
            // There could be the case that a user disconnected, the update took longer but is connected again.
            // The user would be back online but for removal
            final Optional<User> optUser = ctx.users().getUser(idOfDisconnectedUser);
            if (optUser.isPresent())
            {
                final User user = optUser.get();
                if (user.isConnected())
                {
                    forRemoval.add(idOfDisconnectedUser);
                    continue;
                }
            }
            playerLeftIntermediateHandler(idOfDisconnectedUser);
            forRemoval.add(idOfDisconnectedUser);
        }
        userIdsDisconnected.removeAll(forRemoval);
    }

    private void playerLeftUpdate()
    {
        while (!this.userIdsNotifiedForRemove.isEmpty())
        {
            final long userIdLeft = this.userIdsNotifiedForRemove.poll();

            final boolean contained = this.userIdsInHoldForRemove.remove(userIdLeft);
            if (!contained)
            {
                //log.error("UserID for removal but not containing!");
                continue;
            }

            User removedUser = ctx.users().remove(userIdLeft);
            if (removedUser != null && !removedUser.isConnected())
            {
                playerLeftIntermediateHandler(userIdLeft);
            }
        }
    }

    private void playerLeftIntermediateHandler(final long playerId)
    {
        for (SpaceObject value : ctx.spaceObjects().values())
        {
            if (value.getPlayerId() == playerId)
                continue;

            value.getSpaceSubscribeInfo().removeSubscriberWithId(playerId);
        }
    }

    private void objectLeftUpdate()
    {
        final List<ObjectLeftDescription> toSend = new ArrayList<>();
        final List<User> startUsers = ctx.users().getUsers();
        while (!this.objectLeftDescriptions.isEmpty())
        {
            final ObjectLeftDescription desc = this.objectLeftDescriptions.poll();
            toSend.add(desc);
            final SpaceObject spaceObjectToRemove = desc.getRemovedSpaceObject();
            if (spaceObjectToRemove.getSpaceEntityType().isOfType(SpaceEntityType.MiningShip, SpaceEntityType.Outpost))
            {
                spaceObjectToRemove.getSpaceSubscribeInfo().removeAllSubscriber();
            }

            micrometerRegistry.objRemoved(ctx.blueprint().sectorDesc().getSectorID(), spaceObjectToRemove.getSpaceEntityType(), spaceObjectToRemove.getFaction(), desc.getRemovingCause());

            if (desc.getRemovingCause() == RemovingCause.Death)
            {
                if (spaceObjectToRemove.getSpaceEntityType() == SpaceEntityType.Player)
                {
                    final Optional<User> optUser = ctx.users().getUser(spaceObjectToRemove.getPlayerId());
                    if (optUser.isPresent())
                    {
                        final User user = optUser.get();
                        sendRespawnOptions(user);
                        final ObjectLeftDeath leftDeathDesc = (ObjectLeftDeath) desc;
                        if (leftDeathDesc.getKillerObject().isPresent())
                        {
                            final SpaceObject killerObject = leftDeathDesc.getKillerObject().get();

                            final CounterFacade counterFacade = user.getPlayer().getCounterFacade();
                            final long sectorGuid = ctx.blueprint().sectorCards().sectorCard().getCardGuid();
                            counterFacade.incrementCounter(CounterCardType.total_deaths, sectorGuid);
                            if (killerObject.isPlayer())
                            {
                                final TimestampedCounter killedCounter = getKillCounterFaction(spaceObjectToRemove.getFaction());
                                killedCounter.addTimestamp();
                                counterFacade.incrementCounter(CounterCardType.pvp_deaths, sectorGuid);
                            } else
                            {
                                counterFacade.incrementCounter(CounterCardType.pve_deaths, sectorGuid);
                            }
                        }
                    }
                }
            }


            if (spaceObjectToRemove.isPlayer())
            {
                //this.userIdsInHoldForRemove.add(spaceObjectToRemove.getPlayerId());
                final PlayerShip playerShip = (PlayerShip) spaceObjectToRemove;
                final User removedUser = ctx.users().remove(playerShip.getPlayerId());
                //if the user is offline, handle intermediate handler
                if (!removedUser.isConnected())
                {
                    playerLeftIntermediateHandler(playerShip.getPlayerId());
                }
                for (final SpaceObject opOrMiningShip : ctx.spaceObjects().getSpaceObjectsOfEntityTypes(SpaceEntityType.MiningShip, SpaceEntityType.Outpost))
                {
                    opOrMiningShip.getSpaceSubscribeInfo().removeSubscriber(removedUser.getProtocol(ProtocolID.Game));
                }
            }

            this.notifySubscriberObjectLeft(desc);
            this.ctx.spaceObjects().remove(spaceObjectToRemove, desc.getRemovingCause());


            final List<Missile> followMissiles = ctx.spaceObjects().getFollowMissiles(spaceObjectToRemove);
            for (final Missile followMissile : followMissiles)
            {
                updateMissileTargetDead(followMissile);
            }
        }

        if (startUsers.isEmpty())
            return;

        for (final ObjectLeftDescription objectLeftDescription : toSend)
        {
            final BgoProtocolWriter bw = gameProtocolWriteOnly.writeObjectLeft(List.of(objectLeftDescription));
            ctx.sender().sendToClients(bw, startUsers);
        }
    }

    private void sendRespawnOptions(final User user)
    {
        final GameProtocol gameProtocol = user.getProtocol(ProtocolID.Game);
        final boolean sendSuccessfully = gameProtocol.sendRespawnOptions();
        if (!sendSuccessfully)
        {
            //respawn options not good, just dock the player
            final AtomicReference<RespawnOptions> lastRespawnOptions = gameProtocol.getLastRespawnOptions();
            final RespawnOptions respawnOpts = lastRespawnOptions.get();
            if (respawnOpts != null)
            {
                final long respawnSectorId =
                        respawnOpts.sectorIds().isEmpty() ?
                                GalaxyMapCard.getStartSector(user.getPlayer().getFaction()) :
                                respawnOpts.sectorIds().getFirst();

                final Optional<MapStarDesc> optStar = CDI.current().select(Catalogue.class).get().galaxyMapCard().getStar(respawnSectorId);
                optStar.ifPresent(star ->
                {
                    user.getPlayer().getLocation()
                            .setLocation(GameLocation.Room, star.getId(), star.getSectorGuid());
                });
            }
        }
    }

    public TimestampedCounter getKillCounterFaction(final Faction faction)
    {
        return faction == Faction.Colonial ? colonialKilledCounter : cylonKilledCounter;
    }

    private void updateMissileTargetDead(final SpaceObject missile)
    {
        final MovementController movementController = missile.getMovementController();
        movementController.setNextManeuver(new DirectionalManeuver(movementController.getFrame().getEuler3()));
    }
    public void addSubscriber(final ObjectLeftSubscriber subscriber) throws NullPointerException
    {
        Objects.requireNonNull(subscriber, "Subscriber cannot be null!");
        this.subscribers.add(subscriber);
    }

    private void notifySubscriberObjectLeft(final ObjectLeftDescription objectLeftDescription)
    {
        for (final Subscriber<ObjectLeftDescription> subscriber : this.subscribers)
        {
            subscriber.onUpdate(objectLeftDescription);
        }
    }

    private static void logObjectAlreadyRemoved(final SpaceObject spaceObject)
    {
        log.error("Object={} is already removed, cause={}", spaceObject.getObjectID(), spaceObject.getRemovingCause());
    }
}

