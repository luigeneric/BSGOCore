package io.github.luigeneric.core.player;


import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;

import java.time.LocalDateTime;

public class ResourceCap
{
    private final long guid;
    private final int max;
    private int farmed;
    private final BgoTimeStamp lastReset;
    private long lastFarmedValue;

    public ResourceCap(final long guid, final int max, final int farmed, final BgoTimeStamp lastReset)
    {
        this.guid = guid;
        this.max = max;
        this.farmed = farmed;
        this.lastReset = lastReset;
        this.lastFarmedValue = 0;
    }

    public ResourceCap(final long guid, final int max)
    {
        this(guid, max, 0, BgoTimeStamp.now());
    }

    public boolean increaseIfResource(final long guid, final int value)
    {
        if (this.guid != guid)
            return false;

        final long free = this.max - this.farmed;
        final long rv = Math.min(value, free);
        this.lastFarmedValue = rv;
        this.farmed += rv;
        return true;
    }
    public boolean isResource(final long guid)
    {
        return this.guid == guid;
    }

    public long getGuid()
    {
        return guid;
    }

    public int getMax()
    {
        return max;
    }

    public int getFarmed()
    {
        return farmed;
    }

    public BgoTimeStamp getLastReset()
    {
        return lastReset;
    }

    public void resetCap()
    {
        this.farmed = 0;
        this.lastReset.set(System.currentTimeMillis());
    }

    public void setCap(final int value, final LocalDateTime oldCapDate)
    {
        this.farmed = value;
        this.lastReset.set(oldCapDate);
    }

    public long getLastFarmedValue()
    {
        return lastFarmedValue;
    }
}
