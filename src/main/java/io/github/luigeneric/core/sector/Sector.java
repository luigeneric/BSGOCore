package io.github.luigeneric.core.sector;

import io.github.luigeneric.core.LootTemplates;
import io.github.luigeneric.core.sector.collision.CollisionUpdater;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.*;
import io.github.luigeneric.core.sector.management.abilities.AbilityCastRequestQueue;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.sector.management.lootsystem.LootDistributor;
import io.github.luigeneric.core.sector.management.lootsystem.claims.LootClaimHolder;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.sector.management.slots.SectorSlotData;
import io.github.luigeneric.core.sector.management.spawn.SpawnController;
import io.github.luigeneric.core.sector.timers.TimerUpdater;
import io.github.luigeneric.core.sector.zone.SectorZoneManagement;
import io.github.luigeneric.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class Sector implements Runnable
{
    protected final SectorAlgorithms sectorAlgorithms;
    @Getter
    protected final SectorJoinQueue sectorJoinQueue;
    @Getter
    protected final SpaceObjectRemover spaceObjectRemover;
    @Getter
    protected final AbilityCastRequestQueue abilityCastRequestQueue;

    protected boolean sectorIsActive;

    @Getter
    protected final SectorContext ctx;
    @Getter
    protected final DamageMediator damageMediator;
    @Getter
    protected final SectorSlotData sectorSlotData;
    @Getter
    protected final JumpRegistry jumpRegistry;
    protected final TimerUpdater timerUpdater;

    protected final GalaxyBonus galaxyBonus;
    @Getter
    protected final SpawnController spawnController;
    protected final CollisionUpdater collisionUpdater;
    protected final SectorMovementUpdater sectorMovementUpdater;
    protected final LootClaimHolder lootClaimHolder;
    protected final LootDistributor lootDistributor;
    protected final LootTemplates lootTemplates;
    protected final SectorZoneManagement sectorZoneManagement;
    @Getter
    protected final MiningSectorOperations miningSectorOperations;

    public Sector(final SectorContext ctx,
                  final GalaxyBonus galaxyBonus,
                  final LootTemplates lootTemplates, final SectorAlgorithms algorithms,
                  final SpawnController spawnController,
                  final LootDistributor lootDistributor,
                  final LootClaimHolder lootClaimHolder,
                  final SectorJoinQueue joinQueue, final SpaceObjectRemover remover,
                  final DamageMediator damageMediator, final AbilityCastRequestQueue abilityCastRequestQueue,
                  final JumpRegistry jumpRegistry,
                  final TimerUpdater timerUpdater, final CollisionUpdater collisionUpdater,
                  final SectorZoneManagement sectorZoneManagement,
                  final MiningSectorOperations miningSectorOperations
    )
    {
        this.ctx = Objects.requireNonNull(ctx);

        this.galaxyBonus = galaxyBonus;
        this.lootTemplates = lootTemplates;
        this.sectorAlgorithms = algorithms;

        this.lootDistributor = lootDistributor;
        this.sectorJoinQueue = joinQueue;
        this.spawnController = spawnController;
        this.spaceObjectRemover = remover;

        this.sectorIsActive = true;

        this.lootClaimHolder = lootClaimHolder;
        this.damageMediator = damageMediator;
        this.abilityCastRequestQueue = abilityCastRequestQueue;

        this.sectorSlotData = new SectorSlotData();
        this.jumpRegistry = jumpRegistry;
        this.timerUpdater = timerUpdater;
        this.collisionUpdater = collisionUpdater;
        this.sectorZoneManagement = sectorZoneManagement;
        this.miningSectorOperations = miningSectorOperations;
        this.sectorMovementUpdater =
                new SectorMovementUpdater(ctx.tick(), ctx.spaceObjects(), ctx.sender(), this.spaceObjectRemover, this.lootClaimHolder);
    }


    @Override
    public void run()
    {
        //initial set a name for this thread
        Thread.currentThread().setName(Thread.currentThread().getName() + " Sector [" + getId() + "]");

        while (this.sectorIsActive)
        {
            try
            {
                this.ctx.tick().waitForNextTick();

                this.sectorJoinQueue.run();

                this.sectorMovementUpdater.run();

                this.collisionUpdater.run();

                this.abilityCastRequestQueue.run();

                this.timerUpdater.run();

                this.spaceObjectRemover.run();

                this.sectorZoneManagement.run();
            }
            catch (Exception ex)
            {
                log.error("Sector[{}] single crash {}", getId(), Utils.getExceptionStackTrace(ex));
            }
        }
        log.info("Sector[{}] deactivated, isActive-flag: {}", getId(), sectorIsActive);
    }

    public OutpostState getColonialOpState()
    {
        return ctx.outPostStates().colonialOutpostState();
    }

    public OutpostState getCylonOpState()
    {
        return ctx.outPostStates().cylonOutpostState();
    }

    public long getSectorGuid()
    {
        return this.ctx.blueprint().sectorCards().sectorCard().getCardGuid();
    }

    public long getId()
    {
        return ctx.blueprint().sectorDesc().getSectorID();
    }

    public boolean isZone()
    {
        return this.sectorZoneManagement.isZone();
    }



    public LootAssociations getLootAssociations()
    {
        return this.lootClaimHolder.getLootAssociations();
    }

    /**
     * Stops the sector
     * Shutdown
     * turnoff
     */
    public void shutDownSector()
    {
        this.sectorIsActive = false;
        this.ctx.sender().shutdown();
    }
}