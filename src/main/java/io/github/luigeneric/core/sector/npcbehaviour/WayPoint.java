package io.github.luigeneric.core.sector.npcbehaviour;

import io.github.luigeneric.linearalgebra.base.Vector3;

public class WayPoint
{
    private final Vector3 position;
    private final float radiusTrigger;

    public WayPoint(final Vector3 position, float radiusTrigger)
    {
        this.position = position;
        this.radiusTrigger = radiusTrigger;
    }

    public boolean isCloseEnough(final Vector3 toCheck)
    {
        final float sqDist = toCheck.distanceSq(position);
        return sqDist < radiusTrigger * radiusTrigger;
    }
}
