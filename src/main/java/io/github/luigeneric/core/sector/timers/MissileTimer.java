package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;

import java.util.List;

public class MissileTimer extends UpdateTimer
{
    private final ISpaceObjectRemover remover;
    private final Tick tick;
    public MissileTimer(final SectorContext ctx, final ISpaceObjectRemover remover)
    {
        super(ctx.spaceObjects());
        this.remover = remover;
        this.tick = ctx.tick();
    }

    @Override
    public void update(final float dt)
    {
        final List<Missile> missiles = this.sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Missile);
        for (final Missile missile : missiles)
        {
            if (missile.isRemoved())
                continue;

            final boolean ttlOver = missile.getTickSpawnIsAfter(this.tick.getTimeStamp());
            if (ttlOver)
            {
                remover.notifyRemovingCauseAdded(missile, RemovingCause.Death);
            }
        }
    }
}
