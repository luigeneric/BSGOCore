package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.enums.SpaceEntityType;

import java.util.List;

public class LastTimeMiningShipCheck extends DelayedTimer
{
    private final long timeMsPlanetoidWithoutMining;
    public LastTimeMiningShipCheck(final Tick tick,
                                   final SectorSpaceObjects sectorSpaceObjects,
                                   final long delayedTicks,
                                   long timeMsPlanetoidWithoutMining
    )
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.timeMsPlanetoidWithoutMining = timeMsPlanetoidWithoutMining;
    }

    @Override
    protected void delayedUpdate()
    {
        final List<Planetoid> planetoids = this.sectorSpaceObjects
                .getSpaceObjectsOfEntityType(SpaceEntityType.Planetoid);

        for (final Planetoid planetoid : planetoids)
        {
        }
    }
}
