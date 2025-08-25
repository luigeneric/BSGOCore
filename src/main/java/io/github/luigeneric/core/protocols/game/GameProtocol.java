package io.github.luigeneric.core.protocols.game;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.galaxy.Galaxy;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.movement.MovementOptions;
import io.github.luigeneric.core.movement.QWEASD;
import io.github.luigeneric.core.movement.maneuver.*;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.player.location.CICLocation;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.player.location.OutpostLocation;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolMessageHandler;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.community.CommunityProtocolWriteOnly;
import io.github.luigeneric.core.protocols.debug.DebugProtocol;
import io.github.luigeneric.core.protocols.game.handlers.MiningHandler;
import io.github.luigeneric.core.protocols.notification.NotificationProtocol;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.core.protocols.player.UnanchorReason;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.sector.management.JumpRegistry;
import io.github.luigeneric.core.sector.management.OutpostState;
import io.github.luigeneric.core.sector.management.SectorRegistry;
import io.github.luigeneric.core.sector.management.abilities.AbilityCastRequest;
import io.github.luigeneric.core.sector.management.relation.Relation;
import io.github.luigeneric.core.sector.management.relation.RelationUtil;
import io.github.luigeneric.core.spaceentities.CargoObject;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.bindings.PlayerVisibility;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.ShipModifiers;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.StatsProtocolSubscriber;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.utils.AbilityActionType;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.Price;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class GameProtocol extends BgoProtocol implements StatsProtocolSubscriber
{
    private final EnumMap<ClientMessage, ProtocolMessageHandler> handlers;
    private final SectorRegistry sectorRegistry;
    private long jumpTargetSectorID;
    private long jumpTargetSectorGUID;
    private boolean isJumping;
    private boolean isRespawnSend;
    private ScheduledFuture<?> dockFuture;
    private final GameProtocolWriteOnly writer;
    private final AtomicBoolean completeJumpFlag;
    private final AtomicReference<RespawnOptions> lastRespawnOptions;
    private final UsersContainer usersContainer;
    private long[] groupJumpPlayerIds;

    public GameProtocol(final ProtocolContext ctx,
                        final SectorRegistry sectorRegistry,
                        final UsersContainer usersContainer)
    {
        super(ProtocolID.Game, ctx);
        this.handlers = new EnumMap<>(ClientMessage.class);
        this.writer = ProtocolRegistryWriteOnly.game();
        this.sectorRegistry = sectorRegistry;
        this.jumpTargetSectorID = -1;
        this.usersContainer = usersContainer;
        this.completeJumpFlag = new AtomicBoolean(false);
        this.lastRespawnOptions = new AtomicReference<>();
    }

    @Override
    protected void setupHandlers()
    {
        this.handlers.put(ClientMessage.Mining, new MiningHandler(ctx.user(), sectorRegistry));
    }

    @Override
    public void injectUser(User user)
    {
        super.injectUser(user);
        handleRespawnIfNotSetToNull();
    }
    private void handleRespawnIfNotSetToNull()
    {
        final RespawnOptions respawnOpts = this.lastRespawnOptions.get();
        this.lastRespawnOptions.set(null);
        if (respawnOpts == null)
            return;

        log.info("handling respawn since last logout: its not selected yet, select first possible respawn sector");

        //respawn options not good, just dock the player
        final long respawnSectorId =
                respawnOpts.sectorIds().isEmpty() ?
                        GalaxyMapCard.getStartSector(user().getPlayer().getFaction()) :
                        respawnOpts.sectorIds().getFirst();

        final Optional<MapStarDesc> optStar = CDI.current().select(Catalogue.class).get().galaxyMapCard().getStar(respawnSectorId);
        optStar.ifPresent(star ->
        {
            user().getPlayer().getLocation()
                    .setLocation(GameLocation.Room, star.getId(), star.getSectorGuid());
        });
    }

    public GameProtocolWriteOnly writer()
    {
        return this.writer;
    }

    @Override
    public void onDisconnect()
    {
        super.onDisconnect();
        this.completeJumpFlag.set(false);
        this.jumpTargetSectorID = -1;
        Optional<Sector> optSector = getSector();
        if (optSector.isEmpty())
            return;
        Sector sector = optSector.get();
        sector.getSpaceObjectRemover().notifyUserDisconnected(this.userId);
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.valueOf(msgType);

        switch (clientMessage)
        {
            case RequestAnchor ->
            {
                final long targetId = br.readUint32();
                // ignore this for now
                log.warn("RequestAnchor called but not implemented yet");
                if (true)
                    return;

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                final Sector sector = secPlayerShip.sector();
                final PlayerShip playerShip = secPlayerShip.playerShip();

                if (sector == null || playerShip == null || playerShip.isRemoved())
                {
                    log.warn("sector or playership is null or ship is removed {}, {}", sector == null, playerShip == null);
                    return;
                }

                final Optional<IParty> optParty = user().getPlayer().getParty();
                if (optParty.isEmpty())
                {
                    log.error("RequestAnchor but user has no party!");
                    return;
                }
                final IParty partyOfUser = optParty.get();

                final Optional<SpaceObject> targetSpaceObjectCarrier = sector.getCtx().spaceObjects().get(targetId);
                if (targetSpaceObjectCarrier.isEmpty())
                {
                    log.warn("Target to dock is null");
                    return;
                }
                final SpaceObject targetToDock = targetSpaceObjectCarrier.get();
                if (targetToDock.isRemoved())
                {
                    log.warn("Target spaceObject to dock is removed! {}", user().getUserLog());
                    return;
                }
                final Optional<User> optUserToDockAt = usersContainer.get(targetToDock.getPlayerId());
                if (optUserToDockAt.isEmpty())
                {
                    log.error("User to dock at is not present");
                    return;
                }
                final User userToDockAt = optUserToDockAt.get();
                final Optional<IParty> optPartyOfUserToDockAt = userToDockAt.getPlayer().getParty();
                if (optPartyOfUserToDockAt.isEmpty())
                {
                    log.error("Party of user to dock at is not present");
                    return;
                }
                final IParty partyOfUserToDockAt = optPartyOfUserToDockAt.get();
                final boolean isInSameParty = partyOfUserToDockAt.isInParty(playerShip.getPlayerId());
                if (!isInSameParty)
                {
                    log.error("Cheat, user to dock at={} is not in the same party as you={}", userToDockAt.getUserLog(), user().getUserLog());
                    user().send(ProtocolRegistryWriteOnly.writeDebugMessage("Cannot dock on user not in the same party, report this"));
                    return;
                }

                final OwnerCard ownerCard = targetToDock.getOwnerCard();
                final float actualDistance = playerShip.getMovementController()
                        .getPosition()
                        .distance(targetToDock.getMovementController().getPosition());
                if (!ownerCard.isDockable() || ownerCard.getDockRange() < actualDistance)
                {
                    log.warn("Target is not dockable or distance is too high dockable={}, distance={}", ownerCard.isDockable(), actualDistance);
                    return;
                }

                final PlayerVisibility playerVisibility = playerShip.getPlayerVisibility();
                playerVisibility.changeVisibility(false, ChangeVisibilityReason.Anchor, targetId);
                targetToDock.getSpaceObjectState().addAnchoredId(playerShip.getPlayerId());
                final PlayerProtocolWriteOnly playerProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Player);
                user().send(playerProtocolWriteOnly.writeAnchor(targetId));
                final CommunityProtocolWriteOnly communityProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Community);
                final BgoProtocolWriter partyAnchorBuffer = communityProtocolWriteOnly.writePartyAnchor(targetId, playerShip.getPlayerId(), true);
                partyOfUser.sendToAllMembers(partyAnchorBuffer);
            }
            case RequestUnanchor ->
            {
                log.info("RequestUnanchor called");

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                final Sector sector = secPlayerShip.sector();
                final PlayerShip playerShip = secPlayerShip.playerShip();

                if (sector == null || playerShip == null || playerShip.isRemoved())
                {
                    log.warn("sector or playership is null or ship is removed {}, {}", sector == null, playerShip == null);
                    return;
                }


                final PlayerVisibility playerVisibility = playerShip.getPlayerVisibility();
                if (playerVisibility.isVisible())
                {
                    log.warn("PlayerVisibility is already visible but set visible to true again??");
                    return;
                }
                final Optional<IParty> optParty = user().getPlayer().getParty();
                if (optParty.isEmpty())
                {
                    log.error("Party is not present so you cant undock");
                    return;
                }
                final IParty party = optParty.get();

                playerVisibility.changeVisibility(true, ChangeVisibilityReason.Anchor);
                final PlayerProtocolWriteOnly playerProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Player);
                user().send(playerProtocolWriteOnly.writeUnAnchor(playerShip.getObjectID(), UnanchorReason.Default));

                final long anchoredObjectIdTo = playerVisibility.getAnchoredObjectId();
                final Optional<SpaceObject> optAnchoredTo = sector.getCtx().spaceObjects().get(anchoredObjectIdTo);
                if (optAnchoredTo.isEmpty())
                {
                    log.warn("Weird bug, anchored to object is not present");
                    return;
                }
                final SpaceObject anchoredTo = optAnchoredTo.get();
                anchoredTo.getSpaceObjectState().removeAnchoredId(playerShip.getPlayerId());
                final Transform anchoredToTransform = anchoredTo.getMovementController().getTransform();
                final Transform newT = new Transform(Vector3.up().add(500), anchoredToTransform.getRotation(), true);
                playerShip.getMovementController().setNextManeuver(new TeleportManeuver(newT.getPosition()));

                final CommunityProtocolWriteOnly communityProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Community);
                final BgoProtocolWriter partyAnchorBuffer = communityProtocolWriteOnly
                        .writePartyAnchor(anchoredTo.getPlayerId(), playerShip.getPlayerId(), false);
                party.sendToAllMembers(partyAnchorBuffer);
            }
            case RequestLaunchStrikes ->
            {
                log.info("RequestLaunchStrikes called");
            }
            case JumpIn ->
            {
                final Optional<Sector> optSector = sectorRegistry.getSectorById(user().getPlayer().getSectorId());
                if (optSector.isEmpty())
                {
                    log.warn("{} JumpIn but Sector is null for id {}", user().getUserLog(), user().getPlayer().getSectorId());
                    return;
                }
                final Sector sector = optSector.get();
                final Optional<User> optUserInSector = sector.getCtx().users().getUser(user().getPlayer().getUserID());
                log.info("optUsrInSector " + optUserInSector);
                //if exists, user is in respawn
                if (lastRespawnOptions.get() != null)
                {
                    // I can't believe I have to check for this crap
                    log.warn("{} cheat jumpIn but user was dead and respawn opts sent", user().getUserLog());
                    return;
                }

                log.info("{} JumpIn {}", user().getUserLog(), sector.getId());
                optSector.get().getSectorJoinQueue().userJoinQueue(user(), this.groupJumpPlayerIds);
                this.groupJumpPlayerIds = null;
            }
            case Jump ->
            {
                final long sectorID = br.readUint32();
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                {
                    log.error(user().getUserLog() + "Jump but no spaceShip and no sector!");
                    return;
                }
                var playerShip = secPlayerShip.playerShip();

                if (playerShip.isRemoved())
                {
                    log.error(user().getUserLog() + " Cheat PlayerShip is removed but jump request received");
                    return;
                }

                log.info(user().getUserLog() + "JumpReqeust for sectorID: " + sectorID);

                final boolean sectorExists = ctx.galaxy().getGalaxyMapCard().getStars().containsKey(sectorID);
                if (!sectorExists)
                {
                    log.error(user().getUserLog() + "JumpRequest: sector doesn't exists! " + sectorID);
                }
                final Optional<MapStarDesc> optStar = ctx.galaxy().getGalaxyMapCard().getStar(sectorID);
                if (optStar.isEmpty())
                {
                    log.error("Error in fetch galaxystar, cannot find sectorID {} in jumpQuery by user {}", sectorID, user().getUserLog());
                    return;
                }
                final MapStarDesc starDestination = optStar.get();
                final Optional<MapStarDesc> optSourceStar = ctx.galaxy().getGalaxyMapCard().getStar(user().getPlayer().getSectorId());
                if (optSourceStar.isEmpty())
                {
                    log.error(user().getUserLog() + "JumpRequest: source-sector doesn't exists! " + user().getPlayer().getSectorId());
                    return;
                }
                final MapStarDesc sourceStar = optSourceStar.get();
                final float ftlJumpRange = playerShip.getSpaceSubscribeInfo().getStat(ObjectStat.FtlRange);
                final float jumpDistance = Vector2.distance(sourceStar.getPosition(), starDestination.getPosition());
                final boolean canJumpDistance = jumpDistance <= ftlJumpRange;
                final boolean canJumpFaction = starDestination.canJumpFaction(playerShip.getFaction());
                if (GalaxyMapCard.isBaseSector(Faction.invert(playerShip.getFaction()), sectorID) || !canJumpFaction || !canJumpDistance)
                {
                    if (!canJumpDistance)
                        log.error(user().getUserLog() + " Cheat user tried to jump while no jump range distance " + jumpDistance + " range " + ftlJumpRange);

                    final NotificationProtocol notificationProtocol = user().getProtocol(ProtocolID.Notification);
                    notificationProtocol.sendJumpSectorNotAllowed();
                    return;
                }

                if (user().getPlayer().isModifiedAssemblyFlag())
                {
                    log.warn("Cheating user tried to jump while modified " + user().getUserLog());
                }

                final float charge = playerShip.getSpaceSubscribeInfo().getStat(ObjectStat.FtlCharge);
                final boolean isInCombat = playerShip.getSpaceSubscribeInfo().isInCombat();

                this.jumpProcedure(secPlayerShip.sector().getJumpRegistry(), sectorID, charge, isInCombat);
            }
            case GroupJump ->
            {
                final long sectorID = br.readUint32();
                final long[] playerIDs = br.readUint32Array();

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                {
                    log.error(user().getUserLog() + "Jump but no spaceShip and no sector!");
                    return;
                }
                var playerShip = secPlayerShip.playerShip();
                var sector = secPlayerShip.sector();

                final Optional<MapStarDesc> optStar = ctx.galaxy().getGalaxyMapCard().getStar(sectorID);
                if (optStar.isEmpty())
                {
                    log.error("Error in fetch galaxystar, cannot find sectorID {} in jumpQuery by user {}", sectorID, user().getUserLog());
                    return;
                }
                final MapStarDesc starDestination = optStar.get();
                final boolean canJump = starDestination.canJumpFaction(playerShip.getFaction());

                if (GalaxyMapCard.isBaseSector(Faction.invert(playerShip.getFaction()), sectorID) || !canJump)
                {
                    final NotificationProtocol notificationProtocol = user().getProtocol(ProtocolID.Notification);
                    notificationProtocol.sendJumpSectorNotAllowed();
                    return;
                }

                groupJump(sector, sectorID, playerIDs);
            }
            case RequestJumpToBeacon ->
            {
                final long sectorID = br.readUint32();
                DebugProtocol debugProtocol = user().getProtocol(ProtocolID.Debug);
                debugProtocol.sendEzMsg("RequestJumpToBeacon is not implemented! " + sectorID);
            }
            case StopJump ->
            {
                log.info(user().getUserLog() + "StopJump call");
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent()) return;
                this.isJumping = false;

                final Player player = this.user().getPlayer();

                secPlayerShip.sector().getJumpRegistry().removeJump(player.getUserID());
            }
            case StopGroupJump ->
            {
                log.info(user().getUserLog() + "Request stop group jump");
                final Optional<IParty> optParty = user().getPlayer().getParty();
                if (optParty.isEmpty())
                    return;

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();

                if (!secPlayerShip.bothPresent())
                {
                    log.warn("StopGroupJump bot no sector nor plaership {}", secPlayerShip);
                    return;
                }

                var sector = secPlayerShip.sector();

                final IParty party = optParty.get();
                final boolean isLeader = party.getLeader().equals(user());
                final BgoProtocolWriter stopBw = isLeader ?
                        writer.writeLeaderStopGroupJump() :
                        writer.writeStopGroupJump(user().getPlayer().getUserID());
                for (final User member : party.getMembers())
                {
                    if (isLeader)
                    {
                        sector.getJumpRegistry().removeJump(member.getPlayer().getUserID());
                    }
                    else
                    {
                        if (member.equals(user()))
                        {
                            sector.getJumpRegistry().removeJump(member.getPlayer().getUserID());
                        }
                    }
                    member.send(stopBw);
                }
            }
            case Quit ->
            {
                if (!this.isJumping)
                {
                    log.info("User quit gameprotocol but is jumping: " + user().getUserLog());
                    return;
                }
                final Player player = user().getPlayer();
                player.getLocation().setLocation(GameLocation.Space, jumpTargetSectorID, jumpTargetSectorGUID);
                final SceneProtocol sceneProt = user().getProtocol(ProtocolID.Scene);
                sceneProt.sendLoadNextScene();
                this.jumpTargetSectorID = -1;
                this.jumpTargetSectorGUID = -1;
                this.isJumping = false;
                log.info("User quit gameprotocol {}", user().getUserLog());
            }
            case CompleteJump ->
            {

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                {
                    log.warn("Cheat {} RequestCompleteJump but sector or ship not present", user().getUserLog());
                    return;
                }

                /*
                //the ghost effects
                final BgoProtocolWriter bw = writer.writeChangeVisibility(playerShip.getObjectID(),
                        playerShip.getPlayerVisibility());
                this.sector.getSectorSender().sendToAllClients(bw);
                this.user().send(bw);
                 */
                this.completeJumpFlag.set(true);
            }
            case SelectRespawnLocation ->
            {
                final long respawnSectorID = br.readUint32();
                final long respawnPlayerID = br.readUint32();
                //as of now not implemented
                log.info(user().getUserLog() + "RespawnLocation: " + respawnSectorID + " respawnPlayerID: " + respawnPlayerID);

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();

                var playerShip = secPlayerShip.playerShip();

                log.info("both present result " + secPlayerShip.bothPresent());
                if (secPlayerShip.bothPresent() && !playerShip.isDead())
                {
                    log.warn(user().getUserLog() + " cheat SelectRespawnLocation but ship is not dead");
                    return;
                }

                //check if respawnSector is valid
                final RespawnOptions lastTmpSpawnOpts = this.lastRespawnOptions.get();
                if (lastTmpSpawnOpts == null)
                {
                    log.warn(user().getUserLog() + " cheat user selected respawn opts while not set!");
                    return;
                }
                log.info("lastTmpSpawnOpts " + lastTmpSpawnOpts);
                final boolean selectedRespawnSectorIdIsValid = lastTmpSpawnOpts.sectorIds().contains(respawnSectorID);
                if (!selectedRespawnSectorIdIsValid)
                {
                    log.warn(user().getUserLog() + " cheat user selected respawn opts sectorId not valid");
                    return;
                }

                final Optional<Sector> optSector = this.sectorRegistry.getSectorById(respawnSectorID);
                if (optSector.isEmpty())
                {
                    log.warn(user().getUserLog() + "SelectRespawnLocation but sector empty!");
                    return;
                }
                final Sector sector = optSector.get();

                final Player player = this.user().getPlayer();
                sector.getSpaceObjectRemover().playerSelectedRespawnLocation(user().getPlayer().getUserID());
                player.getLocation().setLocation(GameLocation.Room, respawnSectorID, sector.getSectorGuid());

                dockProcedure(true);
                lastRespawnOptions.set(null);
            }
            case TurnByPitchYawStrikes ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                var sector = secPlayerShip.sector();
                final Vector3 pitchYawRollFactor = br.readVector3();
                final Vector2 strafeDirection = br.readVector2();
                float strafeMagnitude = br.readSingle();
                final Gear currentGear = playerShip.getMovementController().getMovementOptions().getGear();
                final boolean isBoost = currentGear == Gear.Boost;
                final boolean isRCS = currentGear == Gear.RCS;
                float strafeLimiter = 1f;
                if (isBoost)
                {
                    strafeLimiter = 0;
                }
                else if (isRCS)
                {
                    strafeLimiter = 0;
                }

                strafeDirection.setX(strafeDirection.getX() * strafeLimiter);
                strafeDirection.setY(strafeDirection.getY() * strafeLimiter);

                final TurnByPitchYawStrikes turnByPitchYawStrikes = new TurnByPitchYawStrikes(
                        pitchYawRollFactor,
                        strafeDirection,
                        strafeMagnitude);
                if (playerShip == null)
                    return;

                //playerShip.getMovementController().setNextManeuver(turnByPitchYawStrikes);
                setNextManeuverFromProtocol(playerShip, turnByPitchYawStrikes);
            }
            case TurnToDirectionStrikes ->
            {
                final Euler3 directionInput = br.readEuler3();
                final float rollInput = br.readSingle();
                final Vector2 strafeDirection = br.readVector2();

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                var sector = secPlayerShip.sector();
                final TurnToDirectionStrikes turnToDirectionStrikes = new TurnToDirectionStrikes(directionInput, rollInput,
                        strafeDirection.getX(), strafeDirection.getY());

                //playerShip.getMovementController().setNextManeuver(turnToDirectionStrikes);
                setNextManeuverFromProtocol(playerShip, turnToDirectionStrikes);
            }
            case MoveToDirection ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;
                var playerShip = secPlayerShip.playerShip();
                final Euler3 direction = br.readEuler3();
                final DirectionalManeuver directionalManeuver = new DirectionalManeuver(direction);
                //playerShip.getMovementController().setNextManeuver(directionalManeuver);
                setNextManeuverFromProtocol(playerShip, directionalManeuver);
            }
            case MoveToDirectionWithoutRoll ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                final Euler3 direction = br.readEuler3();
                //DirectionalWithoutRollManeuver
                DirectionalWithoutRollManeuver directionalWithoutRollManeuver =
                        new DirectionalWithoutRollManeuver(direction);
                //playerShip.getMovementController().setNextManeuver(directionalWithoutRollManeuver);
                setNextManeuverFromProtocol(playerShip, directionalWithoutRollManeuver);
            }
            case QWEASD ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                final int bitmask = br.readByte();
                final QWEASD qweasd = new QWEASD(bitmask);
                final TurnQweasdManeuver newManeuver = new TurnQweasdManeuver(qweasd);
                //playerShip.getMovementController().setNextManeuver(newManeuver);
                setNextManeuverFromProtocol(playerShip, newManeuver);
            }
            case WASD ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                final int bitmask = br.readByte();
                final QWEASD qweasd = new QWEASD(bitmask);
                final Maneuver turnManeuver = new TurnManeuver(qweasd);
                //this.playerShip.getMovementController().setNextManeuver(turnManeuver);
                setNextManeuverFromProtocol(playerShip, turnManeuver);
            }
            case SetGear ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                final byte gearVal = br.readByte();
                final Gear newGear = Gear.forValue(gearVal);
                if (newGear == null)
                {
                    return;
                }
                final MovementController movementController = playerShip.getMovementController();
                final MovementOptions movementOptions = movementController.getMovementOptions();
                float speed = 0;
                float acceleration = playerShip.getSpaceSubscribeInfo().getStat(ObjectStat.Acceleration);
                switch (newGear)
                {
                    case Regular ->
                    {
                        speed = movementOptions.getThrottleSpeed();
                    }

                    case Boost ->
                    {
                        speed = playerShip.getSpaceSubscribeInfo().getStat(ObjectStat.BoostSpeed);
                        final float accBonus = playerShip.getSpaceSubscribeInfo().getStat(ObjectStat.AccelerationMultiplierOnBoost);
                        acceleration *= accBonus;
                        if (movementController.getCurrentManeuver() != null && movementController.getCurrentManeuver() instanceof TurnByPitchYawStrikes oldTurnByPitchYawStrikes)
                        {
                            Vector2 oldStrafe = oldTurnByPitchYawStrikes.getStrafeDirection().copy();
                            oldStrafe.setX(0);
                            oldStrafe.setY(0);
                            final TurnByPitchYawStrikes newTurnByPitch =
                                    new TurnByPitchYawStrikes(
                                            oldTurnByPitchYawStrikes.getPitchYawRollFactor().copy(),
                                            oldStrafe,
                                            oldTurnByPitchYawStrikes.getStrafeMagnitude()
                                    );
                            playerShip.getMovementController().setNextManeuver(newTurnByPitch);
                        }
                    }
                }
                movementOptions.setAcceleration(acceleration);
                movementOptions.setSpeed(speed);
                movementOptions.setGear(newGear);
                /*
                playerShip.getSpaceSubscribeInfo().removeShipBuff(playerShip.getSpaceSubscribeInfo().getShipBuffs().getOfType(AbilityActionType.Slide).
                        stream().map(ShipModifier::getServerID).collect(Collectors.toList()));
                 */
                final Optional<ShipModifiers> optMods = playerShip.getSpaceSubscribeInfo().getModifiers();
                if (optMods.isPresent())
                {
                    final ShipModifiers mods = optMods.get();
                    final Set<Long> allSlides = mods.getOfType(AbilityActionType.Slide).stream()
                            .map(ShipModifier::getServerID).collect(Collectors.toSet());
                    playerShip.getSpaceSubscribeInfo().removeModifiers(allSlides);
                }

                if (movementController.getCurrentManeuver().getManeuverType() == ManeuverType.Rest)
                {
                    final Euler3 direction = Euler3.fromQuaternion(movementController.getRotation());
                    final Maneuver newManeuver = new DirectionalManeuver(direction);
                    //movementController.setNextManeuver(newManeuver);
                    setNextManeuverFromProtocol(playerShip, newManeuver);
                }
                //movementController.setMovementOptionsNeedUpdate();
                setMovementUd(playerShip);
            }
            case SetSpeed ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                final SpeedMode speedMode = SpeedMode.forValue(br.readByte());
                final float clientSpeed = br.readSingle();
                final MovementController movementController = playerShip.getMovementController();
                final Maneuver maneuver = movementController.getCurrentManeuver();
                final MovementOptions movementOptions = movementController.getMovementOptions();
                final float maxSpeed = playerShip.getSpaceSubscribeInfo().getStat(ObjectStat.Speed);


                final float clientSpeedCleaned = Mathf.clampSafe(clientSpeed, 0, maxSpeed);

                movementOptions.setThrottleSpeed(clientSpeedCleaned); //import if gear is changed
                if (movementOptions.getGear() == Gear.Regular)
                {
                    movementOptions.setSpeed(clientSpeedCleaned);
                }
                //movementController.setMovementOptionsNeedUpdate();
                setMovementUd(playerShip);
                if (maneuver.getManeuverType() == ManeuverType.Rest)
                {
                    final Euler3 direction = Euler3.fromQuaternion(movementController.getRotation());
                    final Maneuver newManeuver = new DirectionalManeuver(direction);
                    //playerShip.getMovementController().setNextManeuver(newManeuver);
                    setNextManeuverFromProtocol(playerShip, newManeuver);
                }
            }

            case ToggleAbilityOn ->
            {
                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                {
                    return;
                }
                var playerShip = secPlayerShip.playerShip();
                var sector = secPlayerShip.sector();

                final int abilityID = br.readUint16();
                final long[] targetIDs = br.readUint32Array();
                //Log2.infoIn("toggle ability on");
                final AbilityCastRequest abilityCastRequest = new AbilityCastRequest(playerShip,
                        abilityID, true, targetIDs);
                sector.getAbilityCastRequestQueue().addAutoCastAbility(abilityCastRequest);
            }
            case UpdateAbilityTargets ->
            {
                final int abilityID = br.readUint16();
                final long[] targetIDs = br.readUint32Array();

                var secPlayerShip = getSectorAndPlayerShip();
                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                var sector = secPlayerShip.sector();

                //Log2.infoIn("UPDATE_ABILITY_IDS: " + abilityID + " " + Arrays.toString(targetIDs));
                sector.getAbilityCastRequestQueue().removeAutoCastAbility(abilityID, playerShip.getObjectID());
                final AbilityCastRequest abilityCastRequest = new AbilityCastRequest(playerShip,
                        abilityID, true, targetIDs);
                sector.getAbilityCastRequestQueue().addAutoCastAbility(abilityCastRequest);
            }
            case ToggleAbilityOff ->
            {
                final int abilityID = br.readUint16();

                final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();

                if (!secPlayerShip.bothPresent())
                    return;

                var playerShip = secPlayerShip.playerShip();
                var sector = secPlayerShip.sector();
                sector.getAbilityCastRequestQueue().removeAutoCastAbility(abilityID, playerShip.getObjectID());
            }

            case Dock ->
            {
                var secAndPlayerShip = getSectorAndPlayerShip();
                if (!secAndPlayerShip.bothPresent())
                {
                    log.error("Dock, secandplayership {}", secAndPlayerShip);
                    return;
                }

                var sector = secAndPlayerShip.sector();
                var playerShip = secAndPlayerShip.playerShip();
                final long dockObjectID = br.readUint32();
                final float dockDelay = br.readSingle();
                log.info("Received dock request, dockDelay was {} (useless value)", dockDelay);
                final Optional<SpaceObject> optionalSpaceObject = sector.getCtx().spaceObjects().get(dockObjectID);
                if (optionalSpaceObject.isEmpty())
                {
                    log.warn(user().getUserLog() + "Error docking on target! not existing");
                    return;
                }
                final SpaceObject spaceObjectToDockAt = optionalSpaceObject.get();

                final OwnerCard ownerCard = spaceObjectToDockAt.getOwnerCard();
                //caught cheating
                if (!ownerCard.isDockable())
                {
                    log.error("Caught cheating by docking on nondockable ship " + user().getPlayer().getPlayerLog());
                    return;
                }
                final Vector3 playerPos = playerShip.getMovementController().getPosition();
                final Vector3 dockPos = spaceObjectToDockAt.getMovementController().getPosition();
                final float distance = Vector3.distance(playerPos, dockPos);
                if (distance > ownerCard.getDockRange())
                {
                    log.error("Caught cheating by docking on dockable ship but its too far away " + distance +
                            " " + user().getPlayer().getPlayerLog());
                    return;
                }
                dockProcedure(playerShip.isDead());
            }
            case CancelDocking ->
            {
                final var sectorAndPlayerShip = getSectorAndPlayerShip();
                if (!sectorAndPlayerShip.bothPresent())
                {
                    log.error("CancelDocking not working, playership or sector not present {}", user().getUserLogSimple());
                    return;
                }

                if (this.dockFuture != null)
                {
                    this.dockFuture.cancel(true);
                    sectorAndPlayerShip.playerShip().getSpaceObjectState().setDocking(false);
                }
            }
            case CastImmutableSlotAbility, CastSlotAbility, Mining,
                    LockTarget, MoveInfo, SubscribeInfo, UnSubscribeInfo, WhoIs, Loot ->
            {
                this.parseUpdateMessage(br, clientMessage);
            }
            case CargoInteraction ->
            {
                final long cargoObjectID = br.readUint32();
                final byte rawCargoInteraction = br.readByte();

                final CargoObject.Interaction interaction = CargoObject.Interaction.forValue(rawCargoInteraction);
                if (interaction == null)
                    return;

                log.info("CargoInteraction request: {} {}", cargoObjectID, interaction);
            }

            default -> log.error("GameProtocol unhandled msgtype: " + clientMessage);
        }
    }

    public void jumpProcedure(final JumpRegistry jumpRegistry, final long targetSectorID,
                              final float baseChargeTime, final boolean isInCombat)
    {
        final Player player = this.user().getPlayer();
        final float chargeTime = isInCombat ? baseChargeTime * 4 : baseChargeTime;

        jumpRegistry.addJumpOutRequest(player.getUserID(), targetSectorID, chargeTime, new long[0]);
        this.sendJump(targetSectorID, chargeTime, true);
    }
    public void groupJump(final Sector currentSector, final long targetSectorID, final long[] playerIDs)
    {
        final Optional<IParty> optParty = this.user().getPlayer().getParty();
        if (optParty.isEmpty())
        {
            log.error("Group jump but no party!");
            return;
        }
        final IParty party = optParty.get();
        final Collection<User> partyMembers = party.getMembers();
        final List<User> usersToJump = new ArrayList<>();
        usersToJump.add(party.getLeader());
        for (final User partyMember : partyMembers)
        {
            final Player partyPlayer = partyMember.getPlayer();
            if (partyPlayer.getLocation().getSectorID() == currentSector.getId() &&
                    Arrays.stream(playerIDs).anyMatch(value -> value == partyPlayer.getUserID()))
            {
                usersToJump.add(partyMember);
            }
        }

        //find the highest class

        float chargeTime = 0;
        boolean anyoneInCombat = false;
        for (User userToJump : usersToJump)
        {
            //find charge time
            final Optional<PlayerShip> optionalPlayerShip = currentSector.getCtx().users()
                    .getPlayerShipByUserID(userToJump.getPlayer().getUserID());
            if (optionalPlayerShip.isEmpty())
                continue;
            final PlayerShip playerShip = optionalPlayerShip.get();
            final float playerChargeTime = playerShip.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.FtlCharge);
            chargeTime = Mathf.max(playerChargeTime, chargeTime);

            if (playerShip.getSpaceSubscribeInfo().isInCombat())
                anyoneInCombat = true;
        }
        final float finalChargeTime = anyoneInCombat ? chargeTime * 4 : chargeTime;

        for (final User userToJump : usersToJump)
        {
            final GameProtocol gameProtocol = userToJump.getProtocol(ProtocolID.Game);

            gameProtocol.sendJump(targetSectorID, finalChargeTime, false);
            currentSector.getJumpRegistry().addJumpOutRequest(userToJump.getPlayer().getUserID(), targetSectorID, finalChargeTime, playerIDs);
        }
    }

    public boolean isRespawnSend()
    {
        return isRespawnSend;
    }

    private void parseUpdateMessage(final BgoProtocolReader br, final ClientMessage initalClientMessage) throws IOException
    {
        ClientMessage currentMessage = initalClientMessage;
        boolean isFirstRead = true;

        while (br.canRead())
        {
            if (br.canRead() && !isFirstRead)
            {
                final int msgtype = br.readUint16();
                currentMessage = ClientMessage.valueOf(msgtype);
            }
            isFirstRead = false;

            final ProtocolMessageHandler handler = handlers.get(currentMessage);
            if (handler != null)
            {
                handler.handle(br);
                continue;
            }

            switch (currentMessage)
            {
                //should never happen but to prevent the stream from ending I have to read the bytes and throw them away
                case Loot ->
                {
                    br.readUint32();
                    log.error("Request that should never happened: {} {}", ClientMessage.Loot, user().getUserLog());
                }
                //should never happen2
                case WhoIs ->
                {
                    br.readUint32();
                    log.error("Request that should never happened: {} {}", ClientMessage.WhoIs, user().getUserLog());
                }
                case UnSubscribeInfo ->
                {
                    final long objectID = br.readUint32();
                    final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                    if (!secPlayerShip.hasSector())
                        continue;

                    var sector = secPlayerShip.sector();

                    //Log2.serverInfo("UnSubscribeInfo " + objectID);

                    final Optional<SpaceObject> optObjToUnsupAt = sector.getCtx().spaceObjects().get(objectID);
                    if (optObjToUnsupAt.isEmpty())
                        continue;

                    final SpaceObject objToUnsupAt = optObjToUnsupAt.get();
                    if (objToUnsupAt.isPlayer())
                        continue;

                    objToUnsupAt.getSpaceSubscribeInfo().removeSubscriber(this);
                    //shipStatsInfo.unsubscribe(this);
                }
                case SubscribeInfo ->
                {
                    final long objectID = br.readUint32();

                    var optSector = getSector();
                    if (optSector.isEmpty())
                        continue;
                    var sector = optSector.get();

                    final Optional<SpaceObject> optObjToSubAt = sector.getCtx().spaceObjects().get(objectID);
                    if (optObjToSubAt.isEmpty())
                        continue;

                    final SpaceObject objToSubAt = optObjToSubAt.get();
                    if (objToSubAt.isPlayer())
                    {
                        log.error("PlayerSubscribe request on standard SpaceObject {}", user().getUserLog());
                        continue;
                    }

                    objToSubAt.getSpaceSubscribeInfo().addSubscriber(this);
                    //shipStatsInfo.subscribe(this);
                }
                //should never happen3, but needs to be read from the stream!
                case MoveInfo ->
                {
                    final long[] arr = br.readUint32Array();
                }
                case LockTarget ->
                {
                    var secPlayerShip = this.getSectorAndPlayerShip();
                    final long targetChosenByPlayer = br.readUint32();
                    if (!secPlayerShip.hasPlayerShip())
                        continue;
                    secPlayerShip.playerShip().getSpaceSubscribeInfo().setTargetObjectID(targetChosenByPlayer);
                }
                case CastSlotAbility ->
                {
                    final int abilityID = br.readUint16();
                    final long[] targetIDs = br.readUint32Array();

                    final Player player = user().getPlayer();


                    if (player.getLocation().getGameLocation() != GameLocation.Space)
                    {
                        log.warn(user().getUserLog() + "AbilityCastRequest but user was not in space, he was in " +
                                player.getLocation().getGameLocation());
                        continue;
                    }

                    final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                    var sector = secPlayerShip.sector();

                    if (secPlayerShip.hasPlayerShip())
                    {
                        var slots = secPlayerShip.playerShip().getSpaceSubscribeInfo().getShipSlots();
                        if (slots.isPresent())
                        {
                            ShipSlot slot = slots.get().getSlot(abilityID);
                            log.info("user={}, CastSlotAbility id={}, targetIds={}, slot={}",
                                    user().getUserLog(),
                                    abilityID,
                                    Arrays.toString(targetIDs),
                                    slot
                            );
                        }

                    }

                    final Optional<PlayerShip> optPlayerShip = sector.getCtx().users().getPlayerShipByUserID(player.getUserID());
                    if (optPlayerShip.isPresent())
                    {
                        final PlayerShip playerShip = optPlayerShip.get();
                        final Optional<ShipSlots> optionalShipSlots = playerShip.getSpaceSubscribeInfo().getShipSlots();
                        if (optionalShipSlots.isPresent())
                        {
                            AbilityCastRequest abilityCastRequest = new AbilityCastRequest(playerShip,
                                    abilityID, false, targetIDs);
                            sector.getAbilityCastRequestQueue().addCastRequest(abilityCastRequest);
                        }
                    }
                }
                case CastImmutableSlotAbility ->
                {
                    final int abilityID = br.readUint16();
                    final long[] targetIDs = br.readUint32Array();

                    //Log2.infoIn("CastImmutableSlotAbility: " + abilityID + " "  + Arrays.toString(targetIDs));
                    final Player player = user().getPlayer();

                    final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
                    if (!secPlayerShip.bothPresent())
                    {
                        log.error("CastImmutableSlotAbility both present false {}", secPlayerShip);
                        continue;
                    }

                    var sector = secPlayerShip.sector();

                    final Optional<PlayerShip> optPlayerShip = sector.getCtx().users().getPlayerShipByUserID(player.getUserID());
                    if (optPlayerShip.isEmpty() || targetIDs.length != 1)
                    {
                        continue;
                    }
                    final PlayerShip playerShip = optPlayerShip.get();
                    final Optional<SpaceObject> targetObj = sector.getCtx().spaceObjects().get(targetIDs[0]);


                    targetObj.ifPresent(spaceObject ->
                    {
                        final Relation relation = RelationUtil
                                .getRelation(playerShip, spaceObject, sector.getCtx().blueprint().sectorCards().regulationCard().getTargetBracketMode());

                        if (relation == Relation.Friend)
                        {
                                /*
                                playerShip.getMovementController()
                                        .setNextManeuver(new FollowManeuver(spaceObject));
                                 */
                            setNextManeuverFromProtocol(playerShip, new FollowManeuver(spaceObject));
                        }
                    });
                }
                default ->
                {
                    log.error("Error in UpdateMessage, unknown type {}", currentMessage);
                }
            }
        }
    }

    private void setNextManeuverFromProtocol(final PlayerShip spaceObject, final Maneuver maneuver)
    {
        if (spaceObject == null || maneuver == null)
            return;

        /*
        if (maneuver.getManeuverType() == ManeuverType.TurnByPitchYawStrikes)
        {
            spaceObject.getMovementController().getMovementOptions().deactivateSlideAndBoost();
        }

         */

        spaceObject.getMovementController().setNextManeuver(maneuver);
        spaceObject.getPlayerVisibility().finishGhostJumpInIfNotFinished();
    }
    private void setMovementUd(final PlayerShip spaceObject)
    {
        spaceObject.getMovementController().setMovementOptionsNeedUpdate();
        spaceObject.getPlayerVisibility().finishGhostJumpInIfNotFinished();
    }

    public void dockProcedure(final boolean isAlreadyDead)
    {
        dockProcedure(isAlreadyDead, false);
    }
    public void dockProcedure(final boolean isAlreadyDead, final boolean isAdmin)
    {
        final SectorPlayerShipFetchResult secPlayerShip = getSectorAndPlayerShip();
        if (!secPlayerShip.bothPresent())
        {
            log.info("dockProcedure, secPlayerShip not both present={}, isAlreadyDead={}, isAdmin={}",
                    secPlayerShip, isAlreadyDead, isAdmin
            );
        }
        var playerShip = secPlayerShip.playerShip();
        var sector = secPlayerShip.sector();

        isRespawnSend = false;
        if (this.dockFuture != null && !this.dockFuture.isDone())
        {
            this.dockFuture.cancel(true);
        }

        float dockDelay = 0f;
        if (!isAlreadyDead)
        {
            final boolean isInCombat = playerShip.getSpaceSubscribeInfo().isInCombat();
            final boolean isZeroRule = isAdmin || !isInCombat;
            final int shipTier = playerShip.getShipCard().getTier();
            dockDelay = isZeroRule ? 0 : 10 * shipTier;
        }
        user().send(writer.writeDockingDelay(dockDelay));

        final Runnable runnable = () ->
        {
            if (!isAlreadyDead)
            {
                sector.getSpaceObjectRemover().notifyRemovingCauseAdded(playerShip, RemovingCause.Dock);
            }
            final SceneProtocol sceneProtocol = user().getProtocol(ProtocolID.Scene);
            final Location currentLocation = user().getPlayer().getLocation();
            if (currentLocation.getSectorID() == 0 || currentLocation.getSectorID() == 6)
                currentLocation.changeState(new CICLocation(currentLocation));
            else
            {
                if (isAlreadyDead)
                {
                    currentLocation.changeState(new OutpostLocation(currentLocation, TransSceneType.Die));
                }
                else
                {
                    currentLocation.changeState(new OutpostLocation(currentLocation));
                }
            }
            sceneProtocol.sendLoadNextScene();

            if (isAlreadyDead)
            {
                final var activeStats = user().getPlayer().getHangar()
                        .getActiveShip().getShipStats();
                final float maxHP = activeStats.getStatOrDefault(ObjectStat.MaxHullPoints);
                final float newHP = maxHP * 0.25f;
                activeStats.setHp(newHP);
            }
        };
        this.dockFuture = ctx.scheduledExecutorService().schedule(runnable, (long) dockDelay, TimeUnit.SECONDS);
        if(!isAlreadyDead)
            playerShip.getSpaceObjectState().setDocking(true);
    }

    private Optional<Sector> getSector()
    {
        if (user() == null)
        {
            return Optional.empty();
        }
        final Player player = user().getPlayer();
        return sectorRegistry.getSectorById(player.getSectorId());
    }
    private Optional<PlayerShip> getPlayerShip(final Sector sector)
    {
        return sector.getCtx().users().getPlayerShipByUserID(user().getPlayer().getUserID());
    }
    private SectorPlayerShipFetchResult getSectorAndPlayerShip()
    {
        final Optional<Sector> optSector = getSector();
        if (optSector.isEmpty())
            return SectorPlayerShipFetchResult.NO_SECTOR;

        var sector = optSector.get();
        var optPlayerShip = getPlayerShip(sector);
        if (optPlayerShip.isEmpty())
            return new SectorPlayerShipFetchResult(sector, null);

        return new SectorPlayerShipFetchResult(sector, optPlayerShip.get());
    }



    public BgoProtocolWriter writeScan(final long objectID, final ItemCountable itemInMinable, final boolean isMinable,
                                       final Price miningPrice, final LocalDateTime coolDown, final long sectorId)
    {
        final BgoProtocolWriter bw = writer.writeScan(objectID, itemInMinable, isMinable, miningPrice, coolDown);
        //gameprotocol checkup for mining

        sectorRegistry.getSectorById(sectorId).ifPresent(sector -> sector.getMiningSectorOperations().addMiningRequest(userId, objectID, miningPrice));
        return bw;
    }

    public void sendJump(final long sectorID, final float cooldown, final boolean soloJump)
    {
        this.jumpTargetSectorID = sectorID;
        final MapStarDesc star = ctx.galaxy().getGalaxyMapCard().getStars().get(sectorID);
        if (star == null)
        {
            log.error("Cannot find sector "+sectorID + " to jump to!");
            return;
        }
        this.jumpTargetSectorGUID = star.getSectorGuid();
        this.isJumping = true;

        final BgoProtocolWriter bw = writer.writeJump(cooldown, soloJump, ctx.galaxy().getGalaxyMapCard().getStars().get(sectorID).getSectorGuid());

        this.user().send(bw);
    }

    public boolean sendRespawnOptions()
    {
        if (user() == null || !this.user().isConnected())
        {
            boolean isUserNull = user() == null;
            boolean isUserDisconnected = false;
            if (!isUserNull)
            {
                isUserDisconnected = user().isConnected();
            }

            log.error("SendRespawnOptions but user was null={}, connected={}", isUserNull, isUserDisconnected);
            return false;
        }
        final Player player = this.user().getPlayer();
        this.isRespawnSend = true;
        final List<Long> carrierIDs = new ArrayList<>();
        final List<Long> respawnLocations = new ArrayList<>();

        final Map<Long, MapStarDesc> stars = ctx.galaxy().getGalaxyMapCard().getStars();
        final MapStarDesc mySector = stars.get(player.getSectorId());
        final Faction enemyFaction = user().getPlayer().getFaction().enemyFaction();

        final List<MapStarDesc> sorted = stars.values().stream()
                //remove all sectors that are enemy base sectors
                .filter(Predicate.not(mapStarDesc -> GalaxyMapCard.isBaseSector(enemyFaction, mapStarDesc.getId())))
                .sorted((o1, o2) ->
        {
            final Vector2 vec1 = Vector2.sub(mySector.getPosition(), o1.getPosition());
            final Vector2 vec2 = Vector2.sub(mySector.getPosition(), o2.getPosition());
            final float mag1 = vec1.magnitude();
            final float mag2 = vec2.magnitude();
            return Float.compare(mag1, mag2);
        }).toList();

        for (final MapStarDesc mapStarDesc : sorted)
        {
            if (respawnLocations.size() >= 2)
            {
                break;
            }

            final Optional<Sector> optSector = sectorRegistry.getSectorById(mapStarDesc.getId());
            if (optSector.isEmpty())
                continue;
            final Sector tmpRespawnSector = optSector.get();
            if (GalaxyMapCard.isBaseSector(player.getFaction(), (int) tmpRespawnSector.getId()))
            {
                respawnLocations.add(mapStarDesc.getId());
                carrierIDs.add(0L);
            }
            else
            {
                final OutpostState opState = player.getFaction() == Faction.Colonial ?
                        tmpRespawnSector.getColonialOpState() : tmpRespawnSector.getCylonOpState();
                final boolean isOp = opState.isOutPostCached();
                if (!isOp)
                    continue;
                respawnLocations.add(mapStarDesc.getId());
                carrierIDs.add(0L);
            }
        }
        this.lastRespawnOptions.set(new RespawnOptions(respawnLocations, carrierIDs));
        return user().send(writer.writeSpawnOptions(respawnLocations, carrierIDs));
    }

    public AtomicBoolean getCompleteJumpFlag()
    {
        return completeJumpFlag;
    }

    @Override
    public boolean sendSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer)
    {
        if (user() == null || !user().isConnected())
        {
            log.warn("sending basepropertybuffer even thou user is null");
            return false;
        }

        final BgoProtocolWriter bw = writer.writeSpacePropertyBuffer(spacePropertyBuffer);
        return this.user().send(bw);
    }

    @Override
    public long userId()
    {
        return this.userId;
    }

    public void jumpOutTimerSuccessful(final long[] playerIds)
    {
        this.groupJumpPlayerIds = playerIds;
    }

    public AtomicReference<RespawnOptions> getLastRespawnOptions()
    {
        return lastRespawnOptions;
    }
}
