package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Comet;
import io.github.luigeneric.templates.sectortemplates.CometSectorDesc;

public class CometTimer extends DelayedTimer
{
    private final SpaceObjectFactory factory;
    private final SectorJoinQueue joinQueue;
    private final CometSectorDesc cometSectorDesc;

    public CometTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks,
                      final SpaceObjectFactory factory, SectorJoinQueue joinQueue, final CometSectorDesc cometSectorDesc)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.factory = factory;
        this.joinQueue = joinQueue;
        this.cometSectorDesc = cometSectorDesc;
    }

    @Override
    protected void delayedUpdate()
    {
        if (!cometSectorDesc.activated())
            return;

        for (int i = 0; i < cometSectorDesc.cometCounter(); i++)
        {
            final Comet tmpComet = factory.createComet(23L);
            joinQueue.addSpaceObject(tmpComet);
        }
    }
}
