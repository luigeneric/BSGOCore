package io.github.luigeneric.core.sector.management;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.SpaceObject;

public class JumpScheduleItem implements Comparable<JumpScheduleItem>
{
    private final long timeStamp;
    private final SpaceObject entry;
    private final long targetSector;
    private final long[] playerIds;

    public JumpScheduleItem(long timeStamp, SpaceObject entry, final long targetSector, final long[] playerIds)
    {
        this.timeStamp = timeStamp;
        this.entry = entry;
        this.targetSector = targetSector;
        this.playerIds = playerIds;
    }
    public JumpScheduleItem(final Tick currentTick, final double seconds, SpaceObject entry, final long targetSector, final long[] playerIds)
    {
        this(currentTick.getTimeStamp() + (long) seconds * 1000, entry, targetSector, playerIds);
    }

    public SpaceObject getEntry()
    {
        return entry;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public long getTargetSector()
    {
        return targetSector;
    }

    public long[] getPlayerIds()
    {
        return playerIds;
    }

    @Override
    public int compareTo(JumpScheduleItem o)
    {
        return Long.compare(this.timeStamp, o.timeStamp);
    }

    @Override
    public String toString()
    {
        return "TimeoutTickItem{" +
                "timeStamp=" + timeStamp +
                ", entry=" + entry +
                '}';
    }
}
