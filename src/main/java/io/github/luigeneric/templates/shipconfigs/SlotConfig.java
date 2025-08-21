package io.github.luigeneric.templates.shipconfigs;

public class SlotConfig
{
    protected final int slotID;
    protected final long itemGUID;
    protected final long consumableGUID;

    SlotConfig(int slotID, long itemGUID, long consumableGUID)
    {
        this.slotID = slotID;
        this.itemGUID = itemGUID;
        this.consumableGUID = consumableGUID;
    }

    public int getSlotID()
    {
        return slotID;
    }

    public long getItemGUID()
    {
        return itemGUID;
    }

    public long getConsumableGUID()
    {
        return consumableGUID;
    }
}
