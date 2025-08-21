package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;

public abstract class DelayedTimer extends UpdateTimer
{
    protected final long delayedTicks;
    protected final Tick tick;
    protected Tick lastTick;

    public DelayedTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks)
    {
        super(sectorSpaceObjects);
        this.delayedTicks = delayedTicks;
        this.tick = tick;
        this.lastTick = tick.copy();
    }

    public boolean needsDelay()
    {
        final int tickDelta = tick.getValue() - this.lastTick.getValue();
        if (tickDelta < this.delayedTicks)
        {
            return true;
        }
        this.lastTick = tick.copy();

        return false;
    }

    @Override
    public void update(float dt)
    {
        if (needsDelay())
            return;

        delayedUpdate();
    }

    protected abstract void delayedUpdate();
}
