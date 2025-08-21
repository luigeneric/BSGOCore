package io.github.luigeneric.core.sector.management.slots;

public abstract class SectorSlots
{
    private final long max;
    private long current;


    protected SectorSlots(final long max, final long current)
    {
        this.max = max;
        this.current = current;
    }

    public long getMax()
    {
        return max;
    }

    public void setDeltaCurrent(final long current)
    {
        this.current += current;
    }

    public long getCurrent()
    {
        return current;
    }
}

