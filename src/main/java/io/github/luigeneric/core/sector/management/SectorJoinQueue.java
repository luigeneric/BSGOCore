package io.github.luigeneric.core.sector.management;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.movement.maneuver.RestManeuver;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.SectorJob;
import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.spawn.SpawnArea;
import io.github.luigeneric.core.sector.management.spawn.SpawnAreas;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.collidershapes.Collider;
import io.github.luigeneric.linearalgebra.collidershapes.CollisionRecord;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class SectorJoinQueue implements SectorJob
{
    public static final int MAX_TRANSFORM_AGE_MINUTES = 15;
    private final Deque<SpaceObject> newSpaceObjects;
    private final Deque<NewUserJumpIn> newUsers;
    private final SpawnAreas spawnAreas;
    private final Tick tick;
    private final SectorUsers users;
    private final SectorSpaceObjects spaceObjects;
    private final SectorSender sectorSender;
    private final OutPostStates outpostStates;
    private final SpaceObjectFactory spaceObjectFactory;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;

    public SectorJoinQueue(final Deque<SpaceObject> newSpaceObjects, final Deque<NewUserJumpIn> newUsers,
                           final SpawnAreas spawnAreas, final Tick tick,
                           final SectorUsers users, final SectorSpaceObjects spaceObjects, final SectorSender sectorSender,
                           final OutPostStates outPostStates,
                           final SpaceObjectFactory spaceObjectFactory,
                           final GameProtocolWriteOnly gameProtocolWriteOnly
    )
    {
        this.newSpaceObjects = newSpaceObjects;
        this.newUsers = newUsers;
        this.spawnAreas = spawnAreas;
        this.tick = tick;
        this.users = users;
        this.spaceObjects = spaceObjects;
        this.sectorSender = sectorSender;
        this.outpostStates = outPostStates;
        this.spaceObjectFactory = spaceObjectFactory;
        this.gameProtocolWriteOnly = gameProtocolWriteOnly;
    }
    public SectorJoinQueue(final SpawnAreas spawnAreas, final Tick tick, final SectorUsers users,
                           final SectorSpaceObjects spaceObjects, final SectorSender sectorSender,
                           final OutPostStates outPostStates,
                           final SpaceObjectFactory spaceObjectFactory,
                           final GameProtocolWriteOnly gameProtocolWriteOnly
    )
    {
        this(new ConcurrentLinkedDeque<>(), new ConcurrentLinkedDeque<>(), spawnAreas, tick, users, spaceObjects, sectorSender, outPostStates,
                spaceObjectFactory, gameProtocolWriteOnly);
    }

    public void userJoinQueue(final User user, long[] groupJumpPlayerIds)
    {
        log.info("Adding user to join queue");
        this.newUsers.offer(new NewUserJumpIn(user, groupJumpPlayerIds));
    }


    private void userJoinsSector(final NewUserJumpIn newUserObj) throws IllegalArgumentException
    {
        final User user = newUserObj.user();
        log.info("User joins sector {}", user);
        this.users.removeUserFromJoinFlag(user.getPlayer().getUserID());
        user.send(gameProtocolWriteOnly.writeTimeOrigin(this.tick.getOriginTime()));
        final float colonialDelta = outpostStates.colonialOutpostState().getDelta();
        final float cylonDelta = outpostStates.cylonOutpostState().getDelta();
        user.send(gameProtocolWriteOnly
                .writeOutpostStateBroadcast(outpostStates.colonialOutpostState().getOpPoints(), colonialDelta,
                        outpostStates.cylonOutpostState().getOpPoints(), cylonDelta));

        final Player player = user.getPlayer();

        final Optional<PlayerShip> optPlayerShip = this.users.getPlayerShipByUserID(player.getUserID());
        if (optPlayerShip.isPresent())
        {
            this.sectorSender.sendSpaceObjectsToUser(this.spaceObjects.values(), user);
            this.users.addUserJoinedSpaceObjects(user.getPlayer().getUserID());
        } else
        {
            final PlayerShip newPlayerShip = this.spaceObjectFactory.createPlayerShip(player);

            this.users.add(user, newPlayerShip);
            this.addSpaceObject(newPlayerShip, newUserObj.groupJumpPlayerIds());
        }
    }

    @Override
    public void run()
    {
        while (!newUsers.isEmpty())
        {
            final NewUserJumpIn userToAdd = newUsers.poll();
            userJoinsSector(userToAdd);
        }
        while (!newSpaceObjects.isEmpty())
        {
            final SpaceObject newSpObj = newSpaceObjects.poll();

            //send to all who is
            newSpObj.getSpaceSubscribeInfo().setLastCombatTime(0);

            final GameProtocolWriteOnly gameProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Game);
            final BgoProtocolWriter bw = gameProtocolWriteOnly.writeWhoIs(newSpObj);

            this.sectorSender.sendToAllClients(bw);


            final MovementController movementController = newSpObj.getMovementController();
            try
            {
                movementController.setMovementOptionsStats(newSpObj.getSpaceSubscribeInfo().getStats());
            }
            catch (Exception e)
            {
                log.error("Error setting movement options stats", e);
            }


            //send the new spaceobject (if its first user) all the existing spaceobjects
            if (newSpObj.isPlayer())
            {
                this.users
                        .getUser(newSpObj.getPlayerId())
                        .ifPresent(user -> this.sectorSender.sendSpaceObjectsToUser(spaceObjects.values(), user));
                this.users.addUserJoinedSpaceObjects(newSpObj.getPlayerId());
            }


            //afterwards the creating cause should change to already exists
            newSpObj.setCreatingCause(CreatingCause.AlreadyExists);
            //put to all map
            this.spaceObjects.add(newSpObj);
        }
    }

    public void addSpaceObject(final SpaceObject spaceObject)
    {
        this.addSpaceObject(spaceObject, null);
    }

    public void addSpaceObject(final SpaceObject spaceObject, final long[] groupJumpPlayerIds)
    {
        //check for correct spawnPosition
        initializePlayerSpawn(spaceObject, groupJumpPlayerIds);

        final MovementController movementController = spaceObject.getMovementController();
        spaceObject.getSpaceSubscribeInfo().setMovementUpdateSubscriber(movementController);

        if (!movementController.hasNextManeuver())
        {
            //the current maneuver is null
            final RestManeuver rest = new RestManeuver(movementController.getPosition(),
                    Euler3.fromQuaternion(movementController.getRotation()));
            movementController.setNextManeuver(rest);
        }

        this.newSpaceObjects.offer(spaceObject);
    }

    private void initializePlayerSpawn(SpaceObject spaceObject, final long[] groupJumpPlayerIds)
    {
        if (!spaceObject.isPlayer())
        {
            return;
        }

        final Faction playerFaction = spaceObject.getFaction();
        final Optional<SpawnArea> optSpawn = this.spawnAreas.getSpawnFor(playerFaction);
        if (optSpawn.isEmpty())
        {
            log.error("No spawn area found for faction {}, dropping player into (0,0,0)", playerFaction);
            spaceObject.getMovementController().setNextManeuver(new RestManeuver(new Transform()));
            return;
        }

        final Transform spawnTransform = new Transform();
        final boolean alreadyHasSpawnTransform = applyStoredTransform(spaceObject, spawnTransform);
        if (!alreadyHasSpawnTransform)
        {
            final int MAX_SPAWN_ATTEMPTS = 10;
            final Collider tmpCollider = spaceObject.getCollider().copy();

            for (int i = 0; i < MAX_SPAWN_ATTEMPTS; i++)
            {
                if (i > 0)
                {
                    log.warn("Could not find a valid spawn position for player {}. Retrying attempt {}", spaceObject.getPlayerId(), i);
                }

                spawnTransform.setPositionRotation(
                        optSpawn.get().getRandomPosition(),
                        optSpawn.get().getRotation()
                );

                //check for collisions before accepting transform
                tmpCollider.getTransform().setTransform(spawnTransform);
                tmpCollider.updatePositions();
                // you should not spawn in
                //  asteroids, outposts, platforms, players (missiles are allowed thou)
                final boolean doesCollideWithAny = spaceObjects.getSpaceObjectsNotOfEntityType(
                                SpaceEntityType.Missile
                        )
                        .stream()
                        .filter(SpaceObject::hasCollider)
                        .map(spaceObject1 -> spaceObject1.getCollider().collides(tmpCollider))
                        .filter(Objects::nonNull)
                        .anyMatch(CollisionRecord::collides);
                if (!doesCollideWithAny)
                {
                    break;
                }
            }
        }

        spaceObject.getMovementController().setNextManeuver(new RestManeuver(spawnTransform));
    }

    /**
     * apply old transform
     * @return true if old transform was present and valid
     */
    private boolean applyStoredTransform(final SpaceObject spaceObject, final Transform spawnTransform)
    {
        if (!spaceObject.isPlayer())
            throw new IllegalArgumentException("Only players should have an existing transform");

        final Map<OldUserPositionKey, Transform> transformByOldUserPosition = this.spaceObjects.getTransformByOldUserPosition();
        final OldUserPositionKey oldUserPositionKey = new OldUserPositionKey(spaceObject.getPlayerId(), spaceObject.getFaction());
        if (transformByOldUserPosition.containsKey(oldUserPositionKey) && isUserTransformWithinTimeLimit(oldUserPositionKey))
        {
            final Transform oldPosition = transformByOldUserPosition.get(oldUserPositionKey);
            spawnTransform.setTransform(oldPosition);
            return true;
        }
        return false;
    }
    private boolean isUserTransformWithinTimeLimit(OldUserPositionKey oldUserPositionKey)
    {
        Duration duration = Duration.between(oldUserPositionKey.localDateTime(), LocalDateTime.now(Clock.systemUTC()));
        return duration.toMinutes() < MAX_TRANSFORM_AGE_MINUTES;
    }
}
