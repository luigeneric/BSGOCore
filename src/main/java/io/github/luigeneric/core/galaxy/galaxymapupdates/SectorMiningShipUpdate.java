package io.github.luigeneric.core.galaxy.galaxymapupdates;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorMiningShipUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final int miningShipCount;

    public SectorMiningShipUpdate(final Faction faction, long sectorID, int miningShipCount)
    {
        super(GalaxyUpdateType.SectorMiningShips, faction);
        this.sectorID = sectorID;
        this.miningShipCount = miningShipCount;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(this.faction.value);
        bw.writeInt32(this.miningShipCount);
    }
}
