package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.management.SectorSpaceObjects;

public abstract class UpdateTimer
{
    protected final SectorSpaceObjects sectorSpaceObjects;
    protected boolean stopped;

    public UpdateTimer(final SectorSpaceObjects sectorSpaceObjects)
    {
        this.sectorSpaceObjects = sectorSpaceObjects;
        this.stopped = false;
    }

    public abstract void update(final float dt);

    public void stop()
    {
        this.stopped = true;
    }
}

