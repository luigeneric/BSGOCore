package io.github.luigeneric.core.sector.creation;

import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.core.LootTemplates;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.gameplayalgorithms.UniformDamageDistribution;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.*;
import io.github.luigeneric.core.sector.collision.CollisionResolution;
import io.github.luigeneric.core.sector.collision.CollisionUpdater;
import io.github.luigeneric.core.sector.management.*;
import io.github.luigeneric.core.sector.management.abilities.AbilityCastRequestQueue;
import io.github.luigeneric.core.sector.management.damage.DamageCalculator;
import io.github.luigeneric.core.sector.management.damage.DamageDurabilityModifier;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.sector.management.damage.SectorDamageHistory;
import io.github.luigeneric.core.sector.management.lootsystem.CounterCardDistributor;
import io.github.luigeneric.core.sector.management.lootsystem.LootDistributor;
import io.github.luigeneric.core.sector.management.lootsystem.claims.LootClaimHolder;
import io.github.luigeneric.core.sector.management.lootsystem.killtrace.PvpKillHistory;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.sector.management.notifications.NotificationMediator;
import io.github.luigeneric.core.sector.management.spawn.DynamicNpcSpawn;
import io.github.luigeneric.core.sector.management.spawn.SpawnAble;
import io.github.luigeneric.core.sector.management.spawn.SpawnAreas;
import io.github.luigeneric.core.sector.management.spawn.SpawnController;
import io.github.luigeneric.core.sector.timers.*;
import io.github.luigeneric.core.sector.zone.SectorZoneManagement;
import io.github.luigeneric.core.spaceentities.CruiserShip;
import io.github.luigeneric.core.spaceentities.DebrisPile;
import io.github.luigeneric.core.spaceentities.Planet;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.cards.RegulationCard;
import io.github.luigeneric.templates.cards.SectorCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.colliderstemplates.ColliderTemplates;
import io.github.luigeneric.templates.sectortemplates.BotSpawnTemplate;
import io.github.luigeneric.templates.sectortemplates.MiningShipConfig;
import io.github.luigeneric.templates.sectortemplates.NpcSpawnEntry;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.*;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.templates.zonestemplates.ZoneTemplate;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.Utils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;


@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SectorFactory
{
    private final List<SectorDesc> sectorTemplates;
    private final List<ZoneTemplate> zoneTemplates;
    private final GalaxyBonus galaxyBonus;
    private final ColliderTemplates colliderTemplates;
    private final LootTemplates lootTemplates;
    private final NotificationMediator notificationMediator;
    private final Catalogue catalogue;
    private final MicrometerRegistry micrometerRegistry;
    private final GameServerParamsConfig gameServerParamsConfig;
    private final UsersContainer usersContainer;


    private SectorCards extractSectorCards(final long id)
    {
        final Optional<SectorCard> optSectorCard = catalogue.getSectorCardByID(id);
        if (optSectorCard.isEmpty()) throw new IllegalStateException("SectorCard is null inside sector creation for id: " + id);
        final SectorCard sectorCard = optSectorCard.get();

        final Optional<RegulationCard> optionalRegulationCard = catalogue.fetchCard(sectorCard.getRegulationCardGuid(), CardView.Regulation);
        if (optionalRegulationCard.isEmpty())
            throw new IllegalStateException("Sector-creation, regulationcard was null! " + sectorCard.getRegulationCardGuid());
        final RegulationCard regulationCard = optionalRegulationCard.get();
        final Optional<GalaxyMapCard> optionalGalaxyMapCard = catalogue.fetchCard(StaticCardGUID.GalaxyMap.getValue(), CardView.GalaxyMap);
        if (optionalGalaxyMapCard.isEmpty())
            throw new IllegalStateException("GalaxyMapCard was null");

        return new SectorCards(sectorCard, regulationCard, optionalGalaxyMapCard.get());
    }

    private SectorBlueprint prepareBlueprint(final long id, final GalaxyMapCard galaxyCard)
    {
        final Optional<SectorDesc> optTemplate = sectorTemplates.stream()
                .filter(template -> template.getSectorID() == id)
                .findAny();
        if (optTemplate.isEmpty())
            throw new IllegalArgumentException("SectorTemplate is null inside sector creation!");
        final SectorDesc sectorDesc = optTemplate.get();

        final SectorCards sectorCards = extractSectorCards(id);

        return new SectorBlueprint(sectorDesc, sectorCards, galaxyCard.getStar(id).orElseThrow(() -> new IllegalStateException("Star is null!")));
    }

    /**
     * WIP guess lEgAcY cOdE
     * @return wrapper object OutPostStates hard wired two states (there is only one op each faction)
     */
    private OutPostStates setupOutpostStates(final SectorBlueprint sectorBlueprint, final MapStarDesc starDesc, final Tick tick)
    {
        int coloOpPts = 0;
        int cyloOpPts = 0;
        if (sectorBlueprint.sectorDesc().getSectorID() == 27)
        {
            cyloOpPts = 3000;
        } else if (sectorBlueprint.sectorDesc().getSectorID() == 47)
        {
            coloOpPts = 3000;
        }

        return new OutPostStates(
                new OutpostState(Faction.Colonial, coloOpPts, 3600, starDesc.isCanColonialOutpost(), tick),
                new OutpostState(Faction.Cylon, cyloOpPts, 3600, starDesc.isCanCylonOutpost(), tick),
                sectorBlueprint.sectorDesc().getColonialProgressTemplate(), sectorBlueprint.sectorDesc().getCylonProgressTemplate()
        );
    }

    public Sector createSector(final long id)
    {
        final Tick tick = new Tick(LocalDateTime.now(Clock.systemUTC()).toInstant(ZoneOffset.UTC).toEpochMilli());
        final GameProtocolWriteOnly gameProtocolWriteOnly = ProtocolRegistryWriteOnly.game();

        final Optional<GalaxyMapCard> optGalaxyMapCard = catalogue.fetchCard(StaticCardGUID.GalaxyMap, CardView.GalaxyMap);
        if (optGalaxyMapCard.isEmpty()) throw new IllegalStateException("MapCard is null!");
        final GalaxyMapCard galaxyCard = optGalaxyMapCard.get();

        final MapStarDesc starDesc = galaxyCard.getStars().get(id);
        final OutPostStates opStates = setupOutpostStates(prepareBlueprint(id, galaxyCard), starDesc, tick);

        final SectorBlueprint sectorBlueprint = prepareBlueprint(id, galaxyCard);

        final SectorUsers sectorUsers = new SectorUsers();
        final SectorSpaceObjects sectorSpaceObjects = new SectorSpaceObjects();

        final SpawnAreas spawnAreas = new SpawnAreas();
        sectorBlueprint.sectorDesc().getSpawnAreaTemplates()
                .forEach(spawnAreas::addSpawn);

        final SectorSender sender = new SectorSender(sectorUsers);
        final DamageDurabilityModifier damageDurabilityModifier = new DamageDurabilityModifier(sectorUsers, sender);




        final SectorDamageHistory sectorDamageHistory = new SectorDamageHistory(sectorUsers);


        final ObjectIDRegistry objectIDRegistry = new ObjectIDRegistry();
        final LootAssociations lootAssociations = new LootAssociations();
        final SpaceObjectFactory spaceObjectFactory = new SpaceObjectFactory(
                objectIDRegistry, this.colliderTemplates, sectorBlueprint.sectorDesc(),
                lootAssociations, this.lootTemplates, tick, sectorBlueprint.sectorCards().sectorCard()
        );

        final SectorContext ctx = new SectorContext(tick, sectorUsers, sectorSpaceObjects, new BgoRandom(), sender, sectorBlueprint, objectIDRegistry, spaceObjectFactory, opStates, lootAssociations);



        final SectorJoinQueue sectorJoinQueue = new SectorJoinQueue(spawnAreas, tick, sectorUsers, sectorSpaceObjects, sender,
                opStates, spaceObjectFactory, gameProtocolWriteOnly);
        final SpawnController spawnController = new SpawnController(spawnAreas, sectorBlueprint.sectorDesc(), tick,
                spaceObjectFactory, sectorJoinQueue, lootAssociations, sectorSpaceObjects);
        final SectorOutpostProgress sectorOutpostProgress = new SectorOutpostProgress(tick, opStates, sectorDamageHistory, lootAssociations);



        final SpaceObjectRemover remover = new SpaceObjectRemover(new ConcurrentLinkedDeque<>(), ctx, gameProtocolWriteOnly);

        final SectorAlgorithms sectorAlgorithms = SectorAlgorithms.defaultAlgorithms();
        final JumpRegistry jumpRegistry = new JumpRegistry(sectorUsers, tick);
        final DamageCalculator damageCalculator = new DamageCalculator(sectorAlgorithms, tick, new UniformDamageDistribution());
        final CounterCardDistributor counterCardDistributor = new CounterCardDistributor(sectorUsers, sectorBlueprint.sectorCards().sectorCard());

        final PvpKillHistory pvpKillHistory = new PvpKillHistory();

        final LootDistributor lootDistributor = new LootDistributor(ctx.users(), ctx.bgoRandom(),
                ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Player),
                ctx.tick(), counterCardDistributor, ctx.blueprint(), micrometerRegistry, gameServerParamsConfig, usersContainer, pvpKillHistory);

        final LootClaimHolder lootClaimHolder = new LootClaimHolder(sectorUsers,
                lootAssociations, sectorDamageHistory, lootDistributor, counterCardDistributor);


        final DamageMediator damageMediator = new DamageMediator(ctx, damageCalculator, lootClaimHolder, remover,
                sectorDamageHistory, damageDurabilityModifier, gameProtocolWriteOnly);



        final AbilityCastRequestQueue abilityCastRequestQueue = new AbilityCastRequestQueue(ctx,
                sectorAlgorithms, damageMediator, sectorJoinQueue, lootAssociations);

        final MiningSectorOperations miningShipOperations = new MiningSectorOperations(
                sectorSpaceObjects, lootClaimHolder, sectorJoinQueue, spaceObjectFactory, ctx.blueprint().sectorCards()
        );

        final List<UpdateTimer> timers = this.setupSectorUpdateTimers(ctx,
                remover, ctx.blueprint().sectorDesc().getMiningShipConfig(), lootDistributor, opStates,
                jumpRegistry, spawnController, spaceObjectFactory, sectorJoinQueue,
                abilityCastRequestQueue, sectorDamageHistory, galaxyCard, miningShipOperations
        );
        final TimerUpdater timerUpdater = new TimerUpdater(tick, timers);

        remover.addSubscriber(sectorOutpostProgress);
        remover.addSubscriber(lootClaimHolder);
        remover.addSubscriber(sectorDamageHistory);
        remover.addSubscriber(lootAssociations);
        remover.addSubscriber(spawnController);
        remover.addSubscriber(objectIDRegistry);

        final IntersectionFilter intersectionFilter = new IntersectionFilter(ctx.blueprint().sectorCards().regulationCard());
        final CollisionResolution collisionResolution = new CollisionResolution(damageMediator, remover);
        final CollisionUpdater collisionUpdater = new CollisionUpdater(sectorSpaceObjects, collisionResolution, intersectionFilter, tick);

        final List<BotSpawnTemplate> botSpawnTemplate = ctx.blueprint().sectorDesc().getBotSpawnTemplates();
        if (botSpawnTemplate == null)
        {
            log.info("BotSpawnTemplate is null for sectorId {}", ctx.blueprint().sectorDesc().getSectorID());
        }
        if (botSpawnTemplate != null)
        {
            for (final BotSpawnTemplate spawnTemplate : botSpawnTemplate)
            {
                for (final NpcSpawnEntry npcSpawnEntry : spawnTemplate.npcSpawnEntries())
                {
                    for (int i = 0; i < npcSpawnEntry.count(); i++)
                    {
                        final BotTemplate botTemplate = new BotTemplate(
                                npcSpawnEntry.guid(),
                                SpaceEntityType.BotFighter,
                                CreatingCause.JumpIn, (int) spawnTemplate.respawnTimeDeath(),
                                false,
                                new long[]{npcSpawnEntry.lootId()},
                                spawnTemplate.respawnTimeSeconds(),
                                false,
                                spawnTemplate.lifeTimeSeconds(),
                                spawnTemplate.spawnArea());
                        final DynamicNpcSpawn dynamicNpcSpawn = new DynamicNpcSpawn(spawnController, sectorJoinQueue, spaceObjectFactory, ctx.bgoRandom(), botTemplate);
                        spawnController.enqueue(dynamicNpcSpawn, 0);
                    }
                }
            }
        }

        final Optional<ZoneTemplate> optZoneTemplate = zoneTemplates
                .stream()
                .filter(zoneTemplate -> zoneTemplate.sectorGuid() == ctx.blueprint().sectorCards().sectorCard().getCardGuid())
                .findAny();
        final SectorZoneManagement sectorZoneManagement = new SectorZoneManagement(
                optZoneTemplate.orElse(null)
        );



        final Sector sector = new Sector(
                ctx,
                galaxyBonus,
                lootTemplates,
                sectorAlgorithms,
                spawnController,
                lootDistributor,
                lootClaimHolder,
                sectorJoinQueue,
                remover,
                damageMediator,
                abilityCastRequestQueue,
                jumpRegistry,
                timerUpdater,
                collisionUpdater,
                sectorZoneManagement,
                miningShipOperations
        );

        buildSectorSpaceObjects(sector, ctx.blueprint().sectorDesc());
        this.scheduleSpawnSpaceObjects(sector);

        return sector;
    }

    private List<UpdateTimer> setupSectorUpdateTimers(final SectorContext ctx, final SpaceObjectRemover remover,
                                                      final MiningShipConfig miningShipConfig, final LootDistributor lootDistributor,
                                                      final OutPostStates outPostStates,
                                                      final JumpRegistry jumpRegistry, final SpawnController spawnController,
                                                      final SpaceObjectFactory factory,
                                                      final SectorJoinQueue joinQueue, final AbilityCastRequestQueue abilityCastRequestQueue,
                                                      final SectorDamageHistory sectorDamageHistory, final GalaxyMapCard galaxyMapCard, MiningSectorOperations miningShipOperations)
    {
        final List<UpdateTimer> timers = new ArrayList<>();

        timers.add(new MissileTimer(ctx, remover));
        timers.add(new VisibilityTimer(ctx, remover));
        timers.add(new CombatTimer(ctx.tick(), ctx.spaceObjects(), Utils.timeToTicks(TimeUnit.SECONDS, 1),15));
        timers.add(new MiningShipTimer(ctx, miningShipConfig, galaxyBonus, lootDistributor, remover));
        timers.add(new MiningShipNpcAssassinTimer(ctx, Utils.timeToTicks(TimeUnit.SECONDS, 5f), miningShipConfig, factory, joinQueue));
        timers.add(new JumpInTimer(ctx, Utils.timeToTicks(TimeUnit.SECONDS, 1f)));

        /// TODO I dont want to refactor anymore so this is an assignment for FUTURE-ME C:
        // Future-me after one year yeah ... thats something for (future-me)^2
        var spaceObjects = ctx.spaceObjects();
        var users = ctx.users();
        var tick = ctx.tick();
        var sender = ctx.sender();
        var sectorDesc = ctx.blueprint().sectorDesc();
        var sectorCards = ctx.blueprint().sectorCards();

        timers.add(new JumpTimer(spaceObjects, tick, jumpRegistry, remover, galaxyMapCard, sectorDesc, users));
        timers.add(new HullPointsTimer(spaceObjects));
        timers.add(new PowerPointsTimer(spaceObjects, jumpRegistry, users));
        timers.add(new RecoverComputerBuffTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 1)));
        timers.add(new SpawnScheduler(spaceObjects, spawnController, tick));

        //use only op timers if there is an outpost configuration!
        if (sectorDesc.getColonialProgressTemplate() != null && sectorDesc.getCylonProgressTemplate() != null)
        {
            timers.add(new OutpostDecreaseTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.MINUTES, 7), outPostStates, users, sender));
            timers.add(new OutpostSpawnTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 5), outPostStates, remover, factory, joinQueue, sectorDesc));
            timers.add(new OutpostHpBonusTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.MINUTES, 1), galaxyBonus));
        }
        timers.add(new SpaceObjectStateTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 1), sender));

        timers.add(new SpaceObjectPropertiesTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 0.1f)));
        timers.add(new ShipModifierTimeoutTimer(spaceObjects));
        timers.add(new NpcStaticTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 5),
                abilityCastRequestQueue, sectorDamageHistory, sectorCards));
        timers.add(new NpcDynamicTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 3),
                abilityCastRequestQueue, sectorDamageHistory, remover, sectorCards));
        timers.add(new MovementHeartbeat(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 3), users, sender));
        timers.add(new LogoutTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 10f), users, remover));
        timers.add(new BoostCostTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 1), users, sectorCards.sectorCard()));

        if (sectorDesc.getCometSectorDesc().activated())
        {
            /*
            timers.add(new CometTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, sectorDesc.getCometSectorDesc().delaySeconds()),
                    factory, joinQueue, sectorDesc.getCometSectorDesc()));
             */
        }

        final NotificationTimer notificationTimer = new NotificationTimer(tick, spaceObjects, Utils.timeToTicks(TimeUnit.SECONDS, 7),
                notificationMediator, sectorDamageHistory, sectorCards);
        timers.add(notificationTimer);
        remover.addSubscriber(notificationTimer);

        timers.add(new MiningHistoryCleaner(ctx, Utils.timeToTicks(TimeUnit.MINUTES, 10),miningShipOperations));

        return timers;
    }


    private void scheduleSpawnSpaceObjectCruisers(final Sector sector)
    {
        final SectorDesc sectorDesc = sector.getCtx().blueprint().sectorDesc();
        for (final SpaceObjectTemplate spaceObjectTemplate : sectorDesc.getSpaceObjectTemplates())
        {
            if (spaceObjectTemplate instanceof CruiserTemplate cruiserTemplate)
            {
                final CruiserShip cruiser = sector.getCtx().spaceObjectFactory().createCruiser(cruiserTemplate);
                sector.getSectorJoinQueue().addSpaceObject(cruiser);
            }
        }
    }
    private void scheduleSpawnSpaceObjects(final Sector sector)
    {
        final SectorDesc sectorDesc = sector.getCtx().blueprint().sectorDesc();

        for (final SpaceObjectTemplate spaceObjectTemplate : sectorDesc.getSpaceObjectTemplates())
        {
            if (spaceObjectTemplate.getSpaceEntityType().isOfType(SpaceEntityType.Outpost, SpaceEntityType.Planet, SpaceEntityType.Debris))
            {
                continue;
            }
            final float spawnTime = spaceObjectTemplate.isInstantInSector() ? 0 : getSpawnDelay(spaceObjectTemplate, sectorDesc);
            final SpawnController spawnController = sector.getSpawnController();
            try
            {
                final SpawnAble spawnAble = sector.getSpawnController().createSpawnAble(spaceObjectTemplate);
                spawnController.enqueue(spawnAble, spawnTime);
            } catch (IndexOutOfBoundsException | IllegalArgumentException indexOutOfBoundsException)
            {
                log.warn(indexOutOfBoundsException.getMessage());
            }
        }
    }

    private static float getSpawnDelay(final SpaceObjectTemplate spaceObjectTemplate, final SectorDesc sectorDesc)
    {
        switch (spaceObjectTemplate.getSpaceEntityType())
        {
            case Asteroid ->
            {
                return sectorDesc.getAsteroidDesc().respawnTime();
            }
            case Planetoid ->
            {
                return sectorDesc.getPlanetoidDesc().respawnTime();
            }
            default ->
            {
                return spaceObjectTemplate.getRespawnTime();
            }
        }
    }
    private void buildSectorSpaceObjects(final Sector sector, final SectorDesc sectorDesc)
    {
        final SpaceObjectFactory spaceObjectFactory = sector.getCtx().spaceObjectFactory();
        for (SpaceObjectTemplate spaceObjectTemplate : sectorDesc.getSpaceObjectTemplates())
        {
            if (spaceObjectTemplate.getSpaceEntityType().equals(SpaceEntityType.Asteroid))
            {
                continue;
            }
            if (spaceObjectTemplate.getSpaceEntityType().equals(SpaceEntityType.Planetoid))
            {
                continue;
            }
            if (spaceObjectTemplate.getSpaceEntityType().equals(SpaceEntityType.WeaponPlatform))
            {
                continue;
            }


            switch (spaceObjectTemplate.getSpaceEntityType())
            {
                case Planet ->
                {
                    final PlanetTemplate planetTemplate = (PlanetTemplate) spaceObjectTemplate;
                    final Planet planet = spaceObjectFactory.createPlanet(planetTemplate);
                    sector.getSectorJoinQueue().addSpaceObject(planet);
                }
                case Debris ->
                {
                    final DebrisTemplate debrisTemplate = (DebrisTemplate) spaceObjectTemplate;
                    final DebrisPile debrisPile = spaceObjectFactory.createDebrisPile(debrisTemplate);
                    sector.getSectorJoinQueue().addSpaceObject(debrisPile);
                }
            }
        }
    }
}


