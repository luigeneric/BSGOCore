package io.github.luigeneric.templates.shipitems;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.templates.cards.GuiCard;
import io.github.luigeneric.utils.collections.IServerItem;

public abstract class ShipItem implements IProtocolWrite, IServerItem
{
    protected final long cardGuid;
    @SerializedName(value = "itemType", alternate = "type")
    protected final ItemType itemType;

    @Expose(serialize = false)
    private int serverID;

    protected ShipItem(final long cardGuid, final ItemType itemType, final int serverID)
    {
        this.cardGuid = cardGuid;
        this.itemType = itemType;
        this.serverID = serverID;
    }
    public ShipItem(GuiCard card, ItemType itemType, int serverID)
    {
        this(card.getCardGuid(), itemType, serverID);
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.serverID);
        bw.writeByte(this.itemType.getValue());
        if (this.itemType != ItemType.None)
        {
            bw.writeUInt32(this.cardGuid);
        }
    }

    public abstract ShipItem copy();


    public long getCardGuid()
    {
        return cardGuid;
    }

    public ItemType getItemType()
    {
        return itemType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShipItem shipItem = (ShipItem) o;

        if (cardGuid != shipItem.cardGuid) return false;
        return itemType == shipItem.itemType;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (cardGuid ^ (cardGuid >>> 32));
        result = 31 * result + (itemType != null ? itemType.hashCode() : 0);
        return result;
    }

    @Override
    public int getServerID()
    {
        return this.serverID;
    }
    public void setServerID(int serverID)
    {
        this.serverID = serverID;
    }


    @Override
    public String toString()
    {
        return "ShipItem{" +
                "cardGuid=" + cardGuid +
                ", itemType=" + itemType +
                ", serverID=" + serverID +
                '}';
    }
}
