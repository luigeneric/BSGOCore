package io.github.luigeneric.core.sector.npcbehaviour;


import io.github.luigeneric.linearalgebra.base.Vector3;

import java.util.List;
import java.util.Objects;

/**
 * Npc should move from one waypoint to the next
 */
public class WayPointList
{
    protected final List<WayPoint> wayPoints;
    protected final float minDistanceToWayPoint;
    protected WayPoint currentWayPoint;
    protected int currentIndex;

    public WayPointList(List<WayPoint> wayPoints, float minDistanceToWayPoint)
    {
        Objects.requireNonNull(wayPoints);
        if (wayPoints.size() < 1) throw new IllegalArgumentException("Cannot add waypoints less than 1");
        this.minDistanceToWayPoint = minDistanceToWayPoint;
        this.wayPoints = wayPoints;
        this.currentWayPoint = wayPoints.get(0);
        this.currentIndex = 0;
    }

    public void toNextWayPoint()
    {
        if (!this.isFinished())
            this.currentWayPoint = this.wayPoints.get(++this.currentIndex);
    }

    public boolean isFinished()
    {
        return this.currentIndex == this.wayPoints.size()-1;
    }

    public boolean closeEnoughToWayPoint(final Vector3 currentPos)
    {
        return this.currentWayPoint.isCloseEnough(currentPos);
    }

    public boolean proceedToNextIfCloseEnough(final Vector3 currentPos)
    {
        final boolean isCloseEnough = this.closeEnoughToWayPoint(currentPos);
        if (isCloseEnough)
        {
            this.toNextWayPoint();
        }
        return isCloseEnough;
    }
}
