package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import lombok.Getter;

public class OutpostState
{
    private final Faction faction;
    @Getter
    private int opPoints;
    private final float secondsBlocked;
    private long dieTimeStamp;
    private final boolean canOutpost;
    private boolean isOutPostCached;
    private final Tick tick;


    public OutpostState(
            final Faction faction, final int opPoints, final float secondsBlocked, final boolean canOutpost,
            final Tick tick
    )
    {
        this.faction = faction;
        this.opPoints = opPoints;
        this.secondsBlocked = secondsBlocked;
        this.dieTimeStamp = 0;
        this.canOutpost = canOutpost;
        this.tick = tick;
    }

    public void opDied(final long dieTimeStamp)
    {
        this.dieTimeStamp = dieTimeStamp;
        this.opPoints = 0;
    }
    public void opDied(final Tick tick)
    {
        this.opDied(tick.getTimeStamp());
    }

    public boolean increasePoints(final long deltaIncrease)
    {
        if (this.isBlocked())
        {
            return true;
        }
        this.opPoints += deltaIncrease;
        this.opPoints = Mathf.clampSafe(this.opPoints, 0, 3000);
        return false;
    }
    public boolean decreasePoints(final long deltaDecrease)
    {
        if (this.isBlocked())
            return true;

        this.opPoints -= deltaDecrease;
        this.opPoints = Math.max(this.opPoints, 0);
        return false;
    }

    public boolean isBlocked()
    {
        return this.isBlocked(tick.getTimeStamp());
    }
    public boolean isBlocked(final long currentTimeStamp)
    {
        final long delta = this.getDeltaBlockTime(currentTimeStamp);
        return delta < 0;
    }

    private long getDeltaBlockTime(final long currentTimeStamp)
    {
        final long targetTimeStamp = this.dieTimeStamp + (long)this.secondsBlocked * 1000;
        return currentTimeStamp - targetTimeStamp;
    }

    public float getDelta()
    {
        if (!this.canOutpost)
        {
            return 0f;
        }
        final float rawDelta = this.getDeltaBlockTime(tick.getTimeStamp()) * 0.001f;
        if (rawDelta > 0)
        {
            return this.opPoints >= 900 ? 1 : rawDelta;
        }
        return rawDelta;
    }

    public boolean isOutPost()
    {
        final float currentDelta = this.getDelta();
        this.isOutPostCached = currentDelta == 1f;
        return this.isOutPostCached;
    }

    public boolean isOutPostCached()
    {
        return isOutPostCached;
    }
}
