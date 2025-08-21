package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.MiningSectorOperations;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;

public class MiningHistoryCleaner extends DelayedTimer
{
    private final MiningSectorOperations miningSectorOperations;
    public MiningHistoryCleaner(final SectorContext ctx, long delayedTicks, MiningSectorOperations miningSectorOperations)
    {
        super(ctx.tick(), ctx.spaceObjects(), delayedTicks);
        this.miningSectorOperations = miningSectorOperations;
    }

    @Override
    protected void delayedUpdate()
    {
        miningSectorOperations.clearOld();
    }
}
