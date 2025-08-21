package io.github.luigeneric.core.sector.management.slots;


import io.github.luigeneric.enums.Faction;

public class FactionSectorSlots extends SectorSlots
{
    private final Faction faction;

    protected FactionSectorSlots(long max, long current, Faction faction)
    {
        super(max, current);
        this.faction = faction;
    }

    public Faction getFaction()
    {
        return faction;
    }
}
