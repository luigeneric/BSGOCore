package io.github.luigeneric.core.sector.npcbehaviour;

import java.util.List;

public class CircleWayPoints extends WayPointList
{

    public CircleWayPoints(final List<WayPoint> wayPoints, float minDistanceToWayPoint)
    {
        super(wayPoints, minDistanceToWayPoint);
    }

    @Override
    public boolean isFinished()
    {
        return false;
    }

    @Override
    public void toNextWayPoint()
    {
        if (this.currentIndex >= this.wayPoints.size())
        {
            this.currentIndex = 0;
        }
        else
        {
            this.currentIndex++;
        }
        this.currentWayPoint = this.wayPoints.get(this.currentIndex);
    }
}
