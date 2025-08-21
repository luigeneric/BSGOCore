package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.core.sector.management.slots.SectorSlotData;
import io.github.luigeneric.enums.Faction;

public class SectorPlayerSlotUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final SectorSlotData sectorSlotData;

    public SectorPlayerSlotUpdate(final Faction faction, final long sectorID, final SectorSlotData sectorSlotData)
    {
        super(GalaxyUpdateType.SectorPlayerSlots, faction);
        this.sectorID = sectorID;
        this.sectorSlotData = sectorSlotData;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(faction.value);
        bw.writeDesc(this.sectorSlotData);
    }
}
