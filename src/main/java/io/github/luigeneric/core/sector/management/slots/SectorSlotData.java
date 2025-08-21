package io.github.luigeneric.core.sector.management.slots;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SectorSlotCapType;

import java.util.HashMap;
import java.util.Map;

public class SectorSlotData implements IProtocolWrite
{
    private final FactionSectorSlots colonialSectorSlots;
    private final FactionSectorSlots cylonSectorSlots;
    private final Map<Long, ShipSectorSlots> shipSectorSlotsMap;

    public SectorSlotData(FactionSectorSlots colonialSectorSlots, FactionSectorSlots cylonSectorSlots,
                          Map<Long, ShipSectorSlots> shipSectorSlotsMap)
    {
        this.colonialSectorSlots = colonialSectorSlots;
        this.cylonSectorSlots = cylonSectorSlots;
        this.shipSectorSlotsMap = shipSectorSlotsMap;
    }
    public SectorSlotData()
    {
        this(new FactionSectorSlots(100, 0, Faction.Colonial), new FactionSectorSlots(100, 0, Faction.Cylon),
                new HashMap<>());
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        //length num17
        //specific length + colonial slots + cylon slots + total slots
        int size = shipSectorSlotsMap.size() + 3;
        bw.writeLength(size);

        //start of the loop byte to identify if its shipSlotCap or SectorSlotCapType (Bigpoint what the fuck is wrong with you)
        for (ShipSectorSlots shipSectorSlots : this.shipSectorSlotsMap.values())
        {
            bw.writeByte((byte) 1);
            bw.writeGUID(shipSectorSlots.getGuid());
            bw.writeUInt32(shipSectorSlots.getCurrent());
            bw.writeUInt32(shipSectorSlots.getMax());
        }

        bw.writeByte((byte) 0);
        bw.writeByte(SectorSlotCapType.Colonial.getValue());
        bw.writeUInt32(this.colonialSectorSlots.getCurrent());
        bw.writeUInt32(this.colonialSectorSlots.getMax());

        bw.writeByte((byte) 0);
        bw.writeByte(SectorSlotCapType.Cylon.getValue());
        bw.writeUInt32(this.cylonSectorSlots.getCurrent());
        bw.writeUInt32(this.cylonSectorSlots.getMax());

        bw.writeByte((byte) 0);
        bw.writeByte(SectorSlotCapType.Total.getValue());
        bw.writeUInt32(this.colonialSectorSlots.getCurrent() + this.cylonSectorSlots.getCurrent());
        bw.writeUInt32(this.colonialSectorSlots.getMax() + this.cylonSectorSlots.getMax());
    }
}
