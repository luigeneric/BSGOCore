package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;

public class RingCounter
{
    private final long min;
    private final long max;
    private long nextFreeID;

    public RingCounter(final long min, final long max)
    {
        this.min = min;
        this.max = max;
        this.nextFreeID = min;
    }

    public long getAndIncrementID(final Faction faction, final FactionGroup factionGroup)
    {
        final long freeID = incrementRingID();
        return freeID | faction.mask | factionGroup.mask;
    }
    public long incrementRingID()
    {
        final long freeID = this.nextFreeID;
        this.nextFreeID++;
        if (this.nextFreeID >= this.max)
        {
            this.nextFreeID = this.min;
        }
        return freeID;
    }
}
