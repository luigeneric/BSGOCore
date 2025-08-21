package io.github.luigeneric.templates.utils;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public class ShipImmutableSlot implements IProtocolWrite
{
    @SerializedName("SlotId")
    private final int slotId;
    @SerializedName("ObjectPointServerHash")
    private final int objectPointServerHash;
    /**
     * The ShipSlotType
     */
    @SerializedName("SystemType")
    private final ShipSlotType systemType;
    @SerializedName("SystemKey")
    private final long systemKeyGuid;
    @SerializedName("SystemLevel")
    private final long systemLevel;
    @SerializedName("ConsumableKey")
    private final long consumableKeyGuid;

    public ShipImmutableSlot(int slotId, int objectPointServerHash, ShipSlotType systemType, long systemKeyGuid, long systemLevel, long consumableKeyGuid)
    {
        this.slotId = slotId;
        this.objectPointServerHash = objectPointServerHash;
        this.systemType = systemType;
        this.systemKeyGuid = systemKeyGuid;
        this.systemLevel = systemLevel;
        this.consumableKeyGuid = consumableKeyGuid;
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(slotId);
        bw.writeUInt16(objectPointServerHash);
        bw.writeByte(systemType.getValue());
        bw.writeGUID(systemKeyGuid);
        bw.writeUInt32(systemLevel);
        bw.writeGUID(consumableKeyGuid);
    }
}
