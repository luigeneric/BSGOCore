package io.github.luigeneric.core.spaceentities.statsinfo.stats;

public class HullPowerPoints
{
    private float hullPoints;
    private float powerPoints;

    public HullPowerPoints(final float hullPoints, final float powerPoints)
    {
        this.hullPoints = hullPoints;
        this.powerPoints = powerPoints;
    }

    protected float getHullPoints()
    {
        return hullPoints;
    }

    protected void setHullPoints(final float hullPoints)
    {
        this.hullPoints = hullPoints;
    }

    protected float getPowerPoints()
    {
        return powerPoints;
    }

    protected void setPowerPoints(final float powerPoints)
    {
        this.powerPoints = powerPoints;
    }
}
