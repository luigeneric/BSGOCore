package io.github.luigeneric.core.sector.management.slots;

public class ShipSectorSlots extends SectorSlots
{
    private final long guid;

    protected ShipSectorSlots(long max, long current, long guid)
    {
        super(max, current);
        this.guid = guid;
    }

    public long getGuid()
    {
        return guid;
    }
}
