package io.github.luigeneric.templates.cards;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.templates.utils.ShipSlotType;

public class ShipSlotCard implements IProtocolWrite
{
    @SerializedName("SlotId")
    private final int slotId;
    @SerializedName("ObjectPoint")
    private final String objectPoint;
    @SerializedName("ObjectPointServerHash")
    private final int objectPointServerHash;
    @SerializedName("SystemType")
    private final ShipSlotType shipSlotType;
    @SerializedName("Level")
    private final byte level;

    public ShipSlotCard(int slotId, String objectPoint, int objectPointServerHash, ShipSlotType shipSlotType, byte level)
    {
        this.slotId = slotId;
        this.objectPoint = objectPoint;
        this.objectPointServerHash = objectPointServerHash;
        this.shipSlotType = shipSlotType;
        this.level = level;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(slotId);
        bw.writeString(objectPoint);
        bw.writeUInt16(objectPointServerHash);
        bw.writeByte(shipSlotType.getValue());
        bw.writeByte(level);
    }

    @Override
    public String toString()
    {
        return "ShipSlotCard{" +
                "slotId=" + slotId +
                ", objectPoint='" + objectPoint + '\'' +
                ", objectPointServerHash=" + objectPointServerHash +
                ", shipSlotType=" + shipSlotType +
                ", level=" + level +
                '}';
    }

    public int getSlotId()
    {
        return slotId;
    }

    public String getObjectPoint()
    {
        return objectPoint;
    }

    public int getObjectPointServerHash()
    {
        return objectPointServerHash;
    }

    public ShipSlotType getShipSlotType()
    {
        return shipSlotType;
    }

    public byte getLevel()
    {
        return level;
    }
}
