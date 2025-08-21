package io.github.luigeneric.templates.shipconfigs;

public class ShipConfigTemplate
{
    protected final int id;
    protected final short level;
    protected final long shipGUID;
    protected final SlotConfig[] slotConfigs;

    public ShipConfigTemplate(int id, short level, long shipGUID, SlotConfig[] slotConfigs)
    {
        this.id = id;
        this.level = level;
        this.shipGUID = shipGUID;
        this.slotConfigs = slotConfigs;
    }

    public int getId()
    {
        return id;
    }

    public long getShipGUID()
    {
        return shipGUID;
    }

    public SlotConfig[] getSlotConfigs()
    {
        return slotConfigs;
    }

    public short getLevel()
    {
        return level;
    }
}

