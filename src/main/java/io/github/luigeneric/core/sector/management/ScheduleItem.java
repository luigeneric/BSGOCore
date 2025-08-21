package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.sector.Tick;

public class ScheduleItem<E> implements Comparable<ScheduleItem<E>>
{
    protected final long timeStamp;
    protected final E entry;

    public ScheduleItem(final long timeStamp, final E entry)
    {
        this.timeStamp = timeStamp;
        this.entry = entry;
    }

    public ScheduleItem(final Tick currentTick, final double delaySeconds, E entry)
    {
        this(currentTick.getTimeStamp() + (long) (delaySeconds * 1000.), entry);
    }

    public E getEntry()
    {
        return entry;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }


    @Override
    public int compareTo(ScheduleItem<E> o)
    {
        return Long.compare(this.timeStamp, o.timeStamp);
    }
}
