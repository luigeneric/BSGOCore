package io.github.luigeneric;

import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootSource;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MicrometerRegistry
{
    private final MeterRegistry meterRegistry;
    private final UsersContainer usersContainer;


    @PostConstruct
    public void onInit()
    {
        setupUserCnt();
        setupLevelSum();
    }

    public void wofPlayed(final Faction faction, final long drawCount)
    {
        meterRegistry
                .counter(
                        "game.wof",
                        Tags.of("faction", faction.name()))
                .increment(drawCount);
    }

    public void setMeterRegistrySectorUsers(final Sector sector)
    {
        try
        {
            final SectorUsers sectorUsers = sector.getCtx().users();
            meterRegistry.gauge(
                    "game.sector.users",
                    Tags.of("sectorid", String.valueOf(sector.getId()), "faction", Faction.Colonial.name()),
                    sectorUsers,
                    value -> value.getUserCntBasedOnFaction(Faction.Colonial)
            );
            meterRegistry.gauge(
                    "game.sector.users",
                    Tags.of("sectorid", String.valueOf(sector.getId()), "faction", Faction.Cylon.name()),
                    sectorUsers,
                    value -> value.getUserCntBasedOnFaction(Faction.Cylon)
            );

            for (final SpaceEntityType spaceEntityType : SpaceEntityType.values())
            {
                final SectorSpaceObjects spaceObjects = sector.getCtx().spaceObjects();
                meterRegistry.gauge(
                        "game.sector.spaceobjects",
                        Tags.of("sectorid", String.valueOf(sector.getId()), "spaceentitytype", spaceEntityType.name()),
                        spaceObjects,
                        value -> spaceObjects.getSpaceObjectsOfEntityType(spaceEntityType).size()
                );
            }

            try
            {
                //setup outpost colonial count
                final SectorSpaceObjects spaceObjects = sector.getCtx().spaceObjects();
                meterRegistry.gauge(
                        "game.sector.spaceobjects.outposts",
                        Tags.of("sectorid", String.valueOf(sector.getId()), "faction", Faction.Colonial.name()),
                        spaceObjects,
                        value -> value.getSpaceObjectsOfEntityType(SpaceEntityType.Outpost).stream()
                                .filter(op -> op.getFaction() == Faction.Colonial)
                                .count()
                );

                meterRegistry.gauge(
                        "game.sector.spaceobjects.outposts",
                        Tags.of("sectorid", String.valueOf(sector.getId()), "faction", Faction.Cylon.name()),
                        spaceObjects,
                        value -> value.getSpaceObjectsOfEntityType(SpaceEntityType.Outpost).stream()
                                .filter(op -> op.getFaction() == Faction.Cylon)
                                .count()
                );
            }
            catch (Exception exception)
            {
                log.error("Unknown error", exception);
            }
        }
        catch (Exception exception)
        {
            log.error("Unknown error", exception);
        }

    }

    void setupUserCnt()
    {
        meterRegistry.gauge(
                "game.users",
                Tags.of("faction", Faction.Colonial.name()),
                usersContainer.getColonialCount(),
                AtomicLong::get
        );

        meterRegistry.gauge(
                "game.users",
                Tags.of("faction", Faction.Cylon.name()),
                usersContainer.getCylonCount(),
                AtomicLong::get
        );
    }
    public void setupLevelSum()
    {
        meterRegistry.gauge(
                "game.users.level.sum",
                Tags.of("faction", Faction.Colonial.name()),
                usersContainer.getColonialSumLevel(),
                AtomicLong::get
        );

        meterRegistry.gauge(
                "game.users.level.sum",
                Tags.of("faction", Faction.Cylon.name()),
                usersContainer.getCylonSumLevel(),
                AtomicLong::get
        );
    }

    public void objRemoved(final long sectorID, final SpaceEntityType spaceEntityType, final Faction faction, final RemovingCause removingCause)
    {
        this.meterRegistry.counter(
                "game.objects.removed",
                Tags.of("sectorid", String.valueOf(sectorID),
                        "spaceentitytype", spaceEntityType.name(),
                        "faction", faction.name(),
                        "removingcause", removingCause.name()
                )
        ).increment();
    }
    public void resourceEarned(final long sectorId, final long resourceGuid, final Faction faction, final long amount, final LootSource lootSource)
    {
        this.meterRegistry.counter(
                "game.resource.earned",
                Tags.of(
                        Tag.of("sectorid", String.valueOf(sectorId)),
                        Tag.of("resourceGuid", String.valueOf(resourceGuid)),
                        Tag.of("faction", faction.name()),
                        Tag.of("lootSource", lootSource.name())
                )
        ).increment(amount);
    }

    public void missionSubmitted(final Faction faction)
    {
        this.meterRegistry.counter(
                "game.missions.submitted",
                Tags.of(Tag.of("faction", faction.name()))
        ).increment();
    }
}
