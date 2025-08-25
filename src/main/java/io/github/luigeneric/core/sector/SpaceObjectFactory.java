package io.github.luigeneric.core.sector;

import io.github.luigeneric.core.LootTemplates;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.movement.QWEASD;
import io.github.luigeneric.core.movement.maneuver.TurnManeuver;
import io.github.luigeneric.core.player.AdminRoles;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.player.container.containerids.ShipSlotContainerID;
import io.github.luigeneric.core.sector.management.ObjectIDRegistry;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.sector.management.lootsystem.loot.NpcLoot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.PvpLoot;
import io.github.luigeneric.core.sector.npcbehaviour.DefendObjective;
import io.github.luigeneric.core.sector.npcbehaviour.KillObjective;
import io.github.luigeneric.core.sector.npcbehaviour.NpcObjective;
import io.github.luigeneric.core.sector.npcbehaviour.PatrolObjective;
import io.github.luigeneric.core.spaceentities.*;
import io.github.luigeneric.core.spaceentities.bindings.PlayerVisibility;
import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.bindings.ShipBindings;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.ShipSubscribeInfo;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.Collider;
import io.github.luigeneric.linearalgebra.collidershapes.SphereCollider;
import io.github.luigeneric.templates.cards.*;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.catalogue.WorldOwnerCard;
import io.github.luigeneric.templates.colliderstemplates.ColliderFactory;
import io.github.luigeneric.templates.colliderstemplates.ColliderTemplate;
import io.github.luigeneric.templates.colliderstemplates.ColliderTemplates;
import io.github.luigeneric.templates.loot.LootTemplate;
import io.github.luigeneric.templates.loot.StaticLootId;
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplate;
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplates;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.*;
import io.github.luigeneric.templates.shipconfigs.ShipConfigTemplate;
import io.github.luigeneric.templates.shipconfigs.ShipConfigs;
import io.github.luigeneric.templates.shipconfigs.SlotConfig;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.SpotDesc;
import io.github.luigeneric.templates.utils.SpotType;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.Color;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class SpaceObjectFactory
{
    private final BgoRandom bgoRandom;
    private final ObjectIDRegistry objectIDRegistry;
    private final ColliderTemplates colliderTemplates;
    private final SectorDesc sectorDesc;
    private final LootAssociations lootAssociations;
    private final LootTemplates lootTemplates;
    private final Tick tick;
    private final SectorCard sectorCard;
    private final Catalogue catalogue;

    public SpaceObjectFactory(final ObjectIDRegistry objectIDRegistry, final ColliderTemplates colliderTemplates,
                              final SectorDesc sectorDesc, final LootAssociations lootAssociations,
                              final LootTemplates lootTemplates, final Tick tick,
                              final SectorCard sectorCard
                              )
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.objectIDRegistry = objectIDRegistry;
        this.colliderTemplates = colliderTemplates;
        this.sectorDesc = sectorDesc;
        this.lootAssociations = lootAssociations;
        this.lootTemplates = lootTemplates;
        this.tick = tick;
        this.sectorCard = sectorCard;
        this.bgoRandom = new BgoRandom();
    }

    public Asteroid createAsteroid(final AsteroidTemplate asteroidTemplate, final float calculatedHp)
    {
        final long guid = asteroidTemplate.getObjectGUID();
        final WorldOwnerCard worldOwnerCards = catalogue.fetchWorldOwnerCards(guid);

        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Asteroid, Faction.Neutral);
        final ObjectStats baseStats = new ObjectStats();
        baseStats.setStat(ObjectStat.MaxHullPoints, calculatedHp);
        baseStats.setStat(ObjectStat.MaxPowerPoints, 0f);
        final var stats = new SpaceSubscribeInfo(freeID, baseStats);
        stats.setHpPp(calculatedHp, 0);
        final float radius = asteroidTemplate.getRadius();

        final Asteroid asteroid = new Asteroid(freeID, worldOwnerCards.ownerCard(), worldOwnerCards.worldCard(),
                stats, radius, asteroidTemplate.getRotationSpeed());
        asteroid.createMovementController(asteroidTemplate.getTransform());

        final Collider collider = new SphereCollider(asteroid.getMovementController().getTransform(), Vector3.zero(), radius * 0.9f);
        asteroid.setCollider(collider);
        return asteroid;
    }

    public SpaceObject createComet(final long objectGUID, final Transform spawnTransform)
    {
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Comet, Faction.Neutral);


        final WorldOwnerCard worldOwnerCards = catalogue.fetchWorldOwnerCards(objectGUID);
        final DebrisPile debrisPile = new DebrisPile(freeID, worldOwnerCards.ownerCard(),
                worldOwnerCards.worldCard(), FactionGroup.Group0,
                new SpaceSubscribeInfo(freeID, new ObjectStats()),
                Vector3.one().mult(1), 0
        );
        final Transform transform = new Transform();
        debrisPile.createMovementController(transform);

        return debrisPile;
    }
    public Comet createComet(final long objectGUID)
    {
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Comet, Faction.Neutral);

        var cardWrapper = catalogue.fetchCards(objectGUID, CardView.World, CardView.Owner, CardView.Movement, CardView.NonShipStats);
        final WorldCard worldCard = cardWrapper.getCard(CardView.World);
        final OwnerCard ownerCard = cardWrapper.getCard(CardView.Owner);
        final MovementCard movementCard = cardWrapper.getCard(CardView.Movement);
        final NonShipStatsCard nonShipStatsCard = cardWrapper.getCard(CardView.NonShipStats);


        final SpaceSubscribeInfo stats = new SpaceSubscribeInfo(freeID, nonShipStatsCard.getStats());
        stats.setHp(stats.getStatOrDefault(ObjectStat.MaxHullPoints));
        stats.setPp(stats.getStatOrDefault(ObjectStat.MaxPowerPoints));

        final Comet comet = new Comet(freeID, ownerCard, worldCard, movementCard, stats);

        final float x = bgoRandom.getRndBetweenMinMax(sectorCard.getWidth());
        final float y = bgoRandom.getRndNegPosOf(bgoRandom.getRndBetween(20_000, 30_000));
        final float z = bgoRandom.getRndBetweenMinMax(sectorCard.getLength());

        final float pitch = Math.signum(y) * 80f;
        final float rndYaw = bgoRandom.getRndBetweenMinMax(45);
        final float rndRoll = bgoRandom.getRndBetweenMinMax(45);
        final Euler3 euler3 = new Euler3(pitch, rndYaw, rndRoll);
        final var position = new Vector3(x,y, z);

        comet.createMovementController(new Transform(position, euler3.quaternion()));
        final MovementController movementController = comet.getMovementController();

        final float cometSpeed = comet.getSpaceSubscribeInfo().getStat(ObjectStat.Speed);
        final TurnManeuver turnManeuver = new TurnManeuver(new QWEASD());
        movementController.setNextManeuver(turnManeuver);
        movementController.setMovementOptionsStats(comet.getSpaceSubscribeInfo());
        movementController.getMovementOptions().setGear(Gear.Regular);
        movementController.getMovementOptions().setSpeedAndThrottle(cometSpeed);

        this.colliderTemplates.getColliderTemplate(comet.getPrefabName()).ifPresent(c ->
        {
            final Collider collider = ColliderFactory.fromTemplate(c, movementController.getTransform());
            comet.setCollider(collider);
        });

        lootNpcTemplateSetup(comet, StaticLootId.Comet.value);

        return comet;
    }

    public CruiserShip createCruiser(final CruiserTemplate cruiserTemplate)
    {
        final WorldOwnerCard worldOwnerCard = catalogue.fetchWorldOwnerCards(cruiserTemplate.getObjectGUID());
        final Optional<ShipCard> optionalShipCard = catalogue.fetchCard(cruiserTemplate.getObjectGUID(), CardView.Ship);
        if (optionalShipCard.isEmpty())
            throw new IllegalArgumentException("Cruiser guid is null for ship card in factory! " + cruiserTemplate.getObjectGUID());
        final ShipCard shipCard = optionalShipCard.get();

        final long freeID = this.objectIDRegistry.getFreeObjectId(cruiserTemplate.getSpaceEntityType(), cruiserTemplate.getFaction());
        final ObjectStats stats = new ObjectStats();
        stats.put(shipCard.getStats());

        SpaceSubscribeInfo spaceInfo = new SpaceSubscribeInfo(freeID, stats);

        final CruiserShip cruiserShip = new CruiserShip(freeID, worldOwnerCard.ownerCard(), worldOwnerCard.worldCard(),
                cruiserTemplate.getFaction(), FactionGroup.Group0, new ShipBindings(), new ShipAspects(), spaceInfo, shipCard);


        cruiserShip.createMovementController(cruiserTemplate.getTransform());
        spaceInfo.setMaxHpPp();

        wrapCollider(cruiserShip);

        return cruiserShip;
    }
    public DebrisPile createDebrisPile(final DebrisTemplate debrisTemplate)
    {
        final WorldOwnerCard worldOwnerCards = catalogue.fetchWorldOwnerCards(debrisTemplate.getObjectGUID());

        final long freeID = this.objectIDRegistry.getFreeObjectId(debrisTemplate.getSpaceEntityType(), Faction.Neutral);
        final DebrisPile debrisPile = new DebrisPile(freeID, worldOwnerCards.ownerCard(),
                worldOwnerCards.worldCard(), FactionGroup.Group0,
                new SpaceSubscribeInfo(freeID, new ObjectStats()),
                Vector3.one().mult(debrisTemplate.getScale()), debrisTemplate.getRotationSpeed()
        );
        final Transform transform = debrisTemplate.getTransform();
        debrisPile.createMovementController(transform);

        wrapCollider(debrisPile, debrisTemplate.getScale());

        return debrisPile;
    }


    public DebrisPile createDebrisPile(final long guid)
    {
        return createDebrisPile(guid, Transform.identity());
    }
    public DebrisPile createDebrisPile(final long guid, final Transform transform)
    {
        final WorldOwnerCard worldOwnerCards = catalogue.fetchWorldOwnerCards(guid);

        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Debris, Faction.Neutral);
        final DebrisPile debrisPile = new DebrisPile(freeID, worldOwnerCards.ownerCard(),
                worldOwnerCards.worldCard(), FactionGroup.Group0,
                new SpaceSubscribeInfo(freeID, new ObjectStats()),
                Vector3.one().mult(1), 0
        );
        debrisPile.createMovementController(transform);
        return debrisPile;
    }

    public Planetoid createPlanetoid(final PlanetoidTemplate planetoidTemplate)
    {
        final long objectGUID = planetoidTemplate.getObjectGUID();
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(objectGUID, CardView.World);
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(objectGUID, CardView.Owner);
        if (optOwnerCard.isEmpty() || optWorldCard.isEmpty())
        {
            throw new IllegalArgumentException("Cards for Planetoid missing");
        }
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Planetoid, Faction.Neutral);

        final ObjectStats objStats = new ObjectStats(new HashMap<>());

        objStats.setStat(ObjectStat.MaxHullPoints, 1);
        objStats.setStat(ObjectStat.MaxPowerPoints, 1);
        final SpaceSubscribeInfo stats = new SpaceSubscribeInfo(freeID, objStats);
        stats.setHpPp(1,1);

        final Planetoid planetoid = new Planetoid(freeID, optOwnerCard.get(), optWorldCard.get(),
                stats, planetoidTemplate.getRadius());
        planetoid.createMovementController(planetoidTemplate.getTransform());
        final Collider collider = new SphereCollider(planetoid.getMovementController().getTransform(), Vector3.zero(), 900);
        planetoid.setCollider(collider);
        return planetoid;
    }
    public Planet createPlanet(final long guid, Transform transform)
    {
        return createPlanet(
                new PlanetTemplate(
                        guid,
                        CreatingCause.AlreadyExists,
                        0,
                        true,
                        transform.getPosition(),
                        transform.getRotationEuler3(),
                        1,
                        new Color(0, 0, 0f, 0f),
                        new Color(0f, 0f ,0f, 0f),
                        0)
        );
    }
    public Planet createPlanet(final PlanetTemplate planetTemplate)
    {
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Planet, Faction.Neutral);
        final long objectGUID = planetTemplate.getObjectGUID();
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(objectGUID, CardView.World);
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(objectGUID, CardView.Owner);
        if (optOwnerCard.isEmpty() || optWorldCard.isEmpty())
        {
            throw new IllegalArgumentException("Could not find cardgui!");
        }
        var planet = new Planet(freeID, optOwnerCard.get(), optWorldCard.get(), planetTemplate.getPosition(),
                planetTemplate.getRotation().quaternion(), planetTemplate.getScale(), planetTemplate.getColor(),
                planetTemplate.getSpecularColor(), planetTemplate.getShininess());
        planet.createMovementController(planetTemplate.getTransform());
        return planet;
    }
    public PlayerShip createPlayerShip(final Player player)
    {
        final HangarShip activeShip = player.getHangar().getActiveShip();
        final ShipBindings shipBindings = new ShipBindings();
        final ShipAspects aspects = new ShipAspects();

        final Optional<ShipSlot> optAvionicSlot = activeShip.getShipSlots().getAvionicSlot();
        if (optAvionicSlot.isPresent())
        {
            if (activeShip.getShipCard().getTier() == 1)
                aspects.addAspect(ShipAspect.Dogfight);
        }

        final Optional<ShipSlot> optPaintSlot = activeShip.getShipSlots().getPaintSlot();
        if (optPaintSlot.isPresent())
        {
            final long cardGUID = optPaintSlot.get().getShipSystem().getCardGuid();
            final Optional<ShipSystemPaintCard> optPaintCard = catalogue.fetchCard(cardGUID, CardView.ShipPaint);
            optPaintCard.ifPresent(shipBindings::setShipSystemPaintCard);
        }

        //hide if the user actually has console roles or not!
        final AdminRoles roles = player.getBgoAdminRoles();
        int tmpRoleBits = roles.getRoleBits();
        if (roles.hasRole(BgoAdminRoles.Console))
        {
            tmpRoleBits ^= BgoAdminRoles.Console.value;
        }

        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Player, player.getFaction());
        final PlayerShip playerShip = new PlayerShip(freeID,
                activeShip.getOwnerCard(), activeShip.getWorldCard(), activeShip.getShipCard(),
                player.getFaction(), FactionGroup.extractFactionGroup(freeID), shipBindings, aspects, player.getUserID(),
                tmpRoleBits,
                new PlayerVisibility(false), activeShip.getShipStats());
        final SpaceSubscribeInfo stats = playerShip.getSpaceSubscribeInfo();

        stats.setSkillBook(player.getSkillBook());
        stats.applyStats();
        stats.setPp(activeShip.getShipStats().getPp());
        stats.setHp(activeShip.getShipStats().getHp());

        playerShip.createMovementController(new Transform());

        wrapCollider(playerShip);

        final Optional<ShipSlots> optSlots = playerShip.getSpaceSubscribeInfo().getShipSlots();
        if (optSlots.isPresent())
        {
            final ShipSlots slots = optSlots.get();
            shipBindings.setSlots(slots, playerShip.getShipCard().getTier());
        }

        lootPlayerSetup(playerShip);
        return playerShip;
    }

    public SpaceObject createWeaponPlatform(final WeaponPlatformTemplate weaponPlatformTemplate)
    {
        final long objectGUID = weaponPlatformTemplate.getObjectGUID();
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(objectGUID, CardView.World);
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(objectGUID, CardView.Owner);
        final Optional<ShipCard> optShipCard = catalogue.fetchCard(objectGUID, CardView.Ship);
        if (optOwnerCard.isEmpty() || optWorldCard.isEmpty() || optShipCard.isEmpty())
        {
            throw new IllegalArgumentException("SpaceObjectFactory; could not find cards!");
        }

        //final SpaceEntityType spaceEntityType = weaponPlatformTemplate.getSpaceEntityType();
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.WeaponPlatform, weaponPlatformTemplate.getFaction());
        SpaceSubscribeInfo stats = new ShipSubscribeInfo(freeID, new ObjectStats(optShipCard.get().getStats()));
        stats.setHp(stats.getStat(ObjectStat.MaxHullPoints));
        stats.setPp(stats.getStat(ObjectStat.MaxPowerPoints));

        final Ship weaponPlatform =
                new WeaponPlatform(freeID, optOwnerCard.get(), optWorldCard.get(), optShipCard.get(),
                        weaponPlatformTemplate.getFaction(), stats, NpcBehaviourTemplates.createPlatFormTemplate(optShipCard.get().getTier(), weaponPlatformTemplate),
                        tick.getTimeStamp());
        weaponPlatform.createMovementController(weaponPlatformTemplate.getTransform());

        wrapCollider(weaponPlatform);

        stats.setShipSlots(new ShipSlots());
        setupWeaponConfig(weaponPlatform);



        return lootNpcTemplateSetup(weaponPlatform, weaponPlatformTemplate);
    }

    public SpaceObject createOutpost(final Faction faction) throws IllegalStateException
    {
        final Optional<OutpostTemplate> optOpTemplate = this.sectorDesc.getSpaceObjectTemplates().stream().filter(template -> template.getSpaceEntityType() == SpaceEntityType.Outpost)
                .map(template -> (OutpostTemplate) template)
                .filter(outpostTemplate -> outpostTemplate.getFaction() == faction)
                .findAny();
        if (optOpTemplate.isEmpty())
            throw new IllegalStateException("OP Template for spawn op but cannot find template!");

        final OutpostTemplate opTemplate = optOpTemplate.get();
        final long objectGUID = opTemplate.getObjectGUID();
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(objectGUID, CardView.World);
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(objectGUID, CardView.Owner);
        final Optional<ShipCard> optShipCard = catalogue.fetchCard(objectGUID, CardView.Ship);
        if (optOwnerCard.isEmpty() || optWorldCard.isEmpty() || optShipCard.isEmpty())
        {
            throw new IllegalArgumentException("SpaceObjectFactory; could not find cards!");
        }

        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Outpost, opTemplate.getFaction());

        final SpaceSubscribeInfo stats = new ShipSubscribeInfo(freeID, new ObjectStats(optShipCard.get().getStats()));
        stats.setHp(stats.getStat(ObjectStat.MaxHullPoints));
        stats.setPp(stats.getStat(ObjectStat.MaxPowerPoints));

        final Outpost outpost =
                new Outpost(freeID, optOwnerCard.get(), optWorldCard.get(), optShipCard.get(),
                        opTemplate.getFaction(), stats, NpcBehaviourTemplates.createOutpostTemplate(),
                        this.tick.getTimeStamp(),
                        new ShipAspects()
                );

        outpost.createMovementController(opTemplate.getTransform());
        wrapCollider(outpost);
        stats.setShipSlots(new ShipSlots());
        setupWeaponConfig(outpost);

        return lootNpcTemplateSetup(outpost, opTemplate);
    }

    public SpaceObject createMiningShip(final User user, final Planetoid planetoid)
    {
        final Player player = user.getPlayer();
        //filter for guid!
        final long guid = player.getFaction() == Faction.Colonial ?
                SpaceEntityGUID.ColonialMiningShip.getValue() : SpaceEntityGUID.CylonMiningShip.getValue();

        final Optional<ShipCard> optMiningShipCard = catalogue.fetchCard(guid, CardView.Ship);
        if (optMiningShipCard.isEmpty())
        {
            throw new IllegalArgumentException("Could not find ShipCard in miningShipRequest");
        }
        final ShipCard miningShipCard = optMiningShipCard.get();
        final Optional<OwnerCard> optionalOwnerCard = catalogue.fetchCard(miningShipCard.getCardGuid(), CardView.Owner);
        final Optional<WorldCard> optionalWorldCard = catalogue.fetchCard(miningShipCard.getCardGuid(), CardView.World);
        if (optionalWorldCard.isEmpty() || optionalOwnerCard.isEmpty())
        {
            throw new IllegalArgumentException("Could not find world or owner card in miningShipRequest");
        }
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.MiningShip, player.getFaction());

        ObjectStats miningStats = new ObjectStats();
        miningStats.put(miningShipCard.getStats());
        final var shipStatsInfo = new ShipSubscribeInfo(freeID, miningStats);
        final MiningShip miningShip = new MiningShip(freeID, optionalOwnerCard.get(), optionalWorldCard.get(), player.getFaction(),
                shipStatsInfo, user, planetoid, miningShipCard);

        shipStatsInfo.setHp(shipStatsInfo.getStatOrDefault(ObjectStat.MaxHullPoints) * 0.5f);
        shipStatsInfo.setPp(shipStatsInfo.getStatOrDefault(ObjectStat.MaxPowerPoints) * 0.5f);

        planetoid.setMiningShip(miningShip, tick);

        miningShip.setLastTimeMining(tick.getTimeStamp());

        final Optional<SpotDesc> optMiningSpot = Arrays.stream(planetoid.getWorldCard().getSpots())
                .filter(spot -> spot.getType() == SpotType.Mining).findAny();
        if (optMiningSpot.isEmpty())
            throw new IllegalStateException("Mining ship call but spot was null!");
        final SpotDesc miningSpot = optMiningSpot.get();

        final Transform spotLocalTransform = miningSpot.getLocalTransform();
        final Transform planetoidTransform = planetoid.getMovementController().getTransform();
        final Transform miningRelativeTransform = spotLocalTransform.toGlobalSpaceOf(planetoidTransform);

        miningShip.createMovementController(miningRelativeTransform);
        wrapCollider(miningShip);

        return lootNpcTemplateSetup(miningShip, StaticLootId.MiningShip.value);
    }

    public SpaceObject createMissile(final Ship castingShip, final SpaceObject target, final SpotDesc spotDesc,
                                     final long missileGUID, final long tickSpawnTimeStamp)
    {
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(missileGUID, CardView.Owner);
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(missileGUID, CardView.World);
        final Optional<MovementCard> optMovementCard = catalogue.fetchCard(missileGUID, CardView.Movement);
        final byte casterShipTier = castingShip.getShipCard().getTier();

        if (optMovementCard.isEmpty() || optWorldCard.isEmpty() || optOwnerCard.isEmpty())
        {
            throw new IllegalArgumentException("one of the cards were null for missile");
        }

        final Transform localTransform = spotDesc.getLocalTransform();
        final Transform globalTransform = castingShip.getMovementController().getTransform();
        final Transform relativeTransform = localTransform.toGlobalSpaceOf(globalTransform);

        final OwnerCard ownerCard = optOwnerCard.get();
        final WorldCard worldCard = optWorldCard.get();
        final MovementCard movementCard = optMovementCard.get();
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Missile, castingShip.getFaction());
        final Missile missile = new Missile(freeID, ownerCard, worldCard, movementCard, castingShip.getFaction(),
                castingShip.getFactionGroup(), new SpaceSubscribeInfo(freeID, new ObjectStats()), castingShip, target
                ,casterShipTier, spotDesc.getObjectPointServerHash(), 1f, tickSpawnTimeStamp);
        missile.createMovementController(relativeTransform);
        //setting the collider based on tier
        final byte castingTier = castingShip.getShipCard().getTier();
        final float missileRadius = 10f + 3.5f * castingTier;
        final SphereCollider sphereCollider = new SphereCollider(missile.getMovementController().getTransform(), Vector3.zero(), missileRadius);
        missile.setCollider(sphereCollider);

        if (castingShip.isPlayer())
            lootNpcTemplateSetup(missile, StaticLootId.Missile.value);

        return missile;
    }

    /**
     *
     * @param guid                 type guid such as npc guid
     * @param objectivesToKill     spaceObjects to kill
     * @param objectivesToDefend   spaceObjects to defend
     * @param patrolObjectives     contains an area the npc has to defend
     * @param startPosition        start position of the npc
     * @param npcBehaviourTemplate npc behaviour template
     * @param lootTemplateIDs      templateIds to loot
     * @return a new moving Botfighter object
     */
    public BotFighterMoving createBotFighter(final long guid,
                                             final List<SpaceObject> objectivesToKill,
                                             final List<SpaceObject> objectivesToDefend,
                                             final List<PatrolObjective> patrolObjectives,
                                             final Transform startPosition,
                                             final NpcBehaviourTemplate npcBehaviourTemplate,
                                             final long... lootTemplateIDs)
    {
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(guid, CardView.Owner);
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(guid, CardView.World);
        final Optional<ShipCard> optShipCard = catalogue.fetchCard(guid, CardView.Ship);

        if (optOwnerCard.isEmpty() || optWorldCard.isEmpty() || optShipCard.isEmpty())
        {
            throw new IllegalArgumentException("guid " + guid + " atleast 1 card is missing! " +
                    optOwnerCard.isPresent() + " " + optWorldCard.isPresent() + " " + optShipCard.isPresent());
        }
        final OwnerCard ownerCard = optOwnerCard.get();
        final WorldCard worldCard = optWorldCard.get();
        final ShipCard shipCard = optShipCard.get();


        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.BotFighter, shipCard.getFaction(),
                FactionGroup.Group0);
        final ShipSubscribeInfo spaceSubscribeInfo = new ShipSubscribeInfo(freeID, shipCard.getStats());
        final ArrayList<NpcObjective> fullObjectiveLst = new ArrayList<>()
        {{
            new KillObjective(0, objectivesToKill);
            new DefendObjective(1, objectivesToDefend);
        }};
        fullObjectiveLst.addAll(patrolObjectives);
        final BotFighterMoving botFighterMoving = new BotFighterMoving(freeID, ownerCard, worldCard, shipCard, shipCard.getFaction(), FactionGroup.Group0,
                new ShipBindings(), new ShipAspects(), spaceSubscribeInfo, npcBehaviourTemplate, fullObjectiveLst, this.tick.getTimeStamp());
        botFighterMoving.createMovementController(startPosition);
        spaceSubscribeInfo.setMaxHpPp();
        spaceSubscribeInfo.setShipSlots(new ShipSlots());

        wrapCollider(botFighterMoving);

        this.setupWeaponConfig(botFighterMoving, ownerCard.getLevel());

        return (BotFighterMoving) lootNpcTemplateSetup(botFighterMoving, lootTemplateIDs);
    }

    private void lootPlayerSetup(final PlayerShip playerShip)
    {
        final byte tier = playerShip.getShipCard().getTier();
        final Optional<LootTemplate> optLootTemplate = lootTemplates.get(20 + tier - 1);
        optLootTemplate.ifPresent(lootTemplate ->
        {
            lootAssociations.addLoot(playerShip,
                    new PvpLoot(lootTemplate));
        });

    }
    private SpaceObject lootNpcTemplateSetup(final SpaceObject spaceObject, final long... ids)
    {
        if (ids == null)
            return spaceObject;
        final List<LootTemplate> lootTemplateLst = getTemplateLst(ids);
        lootAssociations.addLoot(spaceObject, new NpcLoot(lootTemplateLst));

        return spaceObject;
    }
    private SpaceObject lootNpcTemplateSetup(final SpaceObject spaceObject, final SpaceObjectTemplate spaceObjectTemplate)
    {
        final long[] lootTemplateIds = spaceObjectTemplate.getLootTemplateIds();
        if (lootTemplateIds == null)
            return spaceObject;
        final List<LootTemplate> lootTemplateLst = getTemplateLst(lootTemplateIds);
        lootAssociations.addLoot(spaceObject, new NpcLoot(lootTemplateLst));

        return spaceObject;
    }
    private void wrapCollider(final SpaceObject spaceObject, final float scale)
    {
        final Optional<ColliderTemplate> optCollider =
                this.colliderTemplates.getColliderTemplate(spaceObject.getPrefabName());
        optCollider.ifPresent(c ->
        {
            spaceObject.setCollider(ColliderFactory.fromTemplate(c, spaceObject.getMovementController().getTransform(), scale));
        });
    }
    private void wrapCollider(final SpaceObject spaceObject)
    {
        wrapCollider(spaceObject, 1);
    }

    public BsgoTrigger createBsgoTrigger()
    {
        final long freeID = this.objectIDRegistry.getFreeObjectId(SpaceEntityType.Trigger, Faction.Neutral);
        final long guid = 39L;
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(guid, CardView.Owner);
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(guid, CardView.World);
        final Optional<MovementCard> optMovementCard = catalogue.fetchCard(guid, CardView.Movement);

        if (optMovementCard.isEmpty() || optWorldCard.isEmpty() || optOwnerCard.isEmpty())
        {
            throw new IllegalArgumentException("one of the cards were null for missile");
        }

        final BsgoTrigger bsgoTrigger = new BsgoTrigger(freeID, optOwnerCard.get(), optWorldCard.get(), SpaceEntityType.Trigger,
                Faction.Neutral, FactionGroup.Group0, new SpaceSubscribeInfo(freeID, new ObjectStats()), "TestTrigger", Vector3.zero(),
                1000);
        bsgoTrigger.createMovementController(new Transform());
        return bsgoTrigger;
    }

    private List<LootTemplate> getTemplateLst(final long... ids)
    {
        return Arrays.stream(ids)
                .mapToObj(lootTemplates::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private void setupWeaponConfig(final Ship ship)
    {
        setupWeaponConfig(ship, (short) -1);
    }
    private void setupWeaponConfig(final Ship ship, final short level)
    {
        final Optional<ShipConfigTemplate> optionalShipConfigTemplate = level == -1 ?
                ShipConfigs.getFirstBestConfigForGUID(ship.getShipCard().getCardGuid()) :
                ShipConfigs.getFirstBestConfigForGUIDAndLevel(ship.getShipCard().getCardGuid(), level);

        if (optionalShipConfigTemplate.isEmpty())
            return;
        final ShipConfigTemplate shipConfigTemplate = optionalShipConfigTemplate.get();
        final var stats = ship.getSpaceSubscribeInfo();
        final Optional<ShipSlots> optSlots = stats.getShipSlots();
        if (optSlots.isEmpty())
        {
            log.error("SpaceObjectFactory: optSlots null for id " + ship.getShipCard().getCardGuid());
            return;
        }
        final ShipSlots slots = optSlots.get();

        for (final ShipSlotCard shipSlotCard : ship.getShipCard().getShipSlotCards())
        {
            final Optional<SlotConfig> optSlotConfig = Arrays.stream(shipConfigTemplate.getSlotConfigs())
                    .filter(slotConfig -> slotConfig.getSlotID() == shipSlotCard.getSlotId())
                    .findAny();
            if (optSlotConfig.isEmpty())
            {
                //Log.error("SpaceObjectFactory: slot config is null for slotid: " + shipSlotCard.getSlotId() + " guid: " + ship.getShipCard().getCardGuid());
                continue;
            }
            final SlotConfig slotConfig = optSlotConfig.get();
            final ShipSlot shipSlot = new ShipSlot(
                    new ShipSlotContainerID(ship.getShipCard().getHangarId(), shipSlotCard.getSlotId()),
                    shipSlotCard
            );
            final ShipSystem shipSystem = ShipSystem.fromGUID(slotConfig.getItemGUID());
            shipSlot.addShipItem(shipSystem);
            if (slotConfig.getConsumableGUID() != 0)
            {
                shipSlot.setCurrentConsumable(ItemCountable.fromGUID(slotConfig.getConsumableGUID(), Long.MAX_VALUE));
            }

            slots.addSlot(shipSlot);
        }
        ship.getShipBindings().setSlots(slots, ship.getShipCard().getTier());
        ship.getSpaceSubscribeInfo().applyStats();
    }

}
