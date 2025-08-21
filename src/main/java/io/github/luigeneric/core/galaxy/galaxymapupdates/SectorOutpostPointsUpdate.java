package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorOutpostPointsUpdate extends GalaxyMapUpdate
{
    private final int outpostPoints;
    private final long sectorId;

    public SectorOutpostPointsUpdate(Faction faction, long sectorId, int outpostPoints)
    {
        super(GalaxyUpdateType.SectorOutpostPoints, faction);
        this.outpostPoints = outpostPoints;
        this.sectorId = sectorId;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorId);
        bw.writeByte(this.faction.value);
        bw.writeInt32(this.outpostPoints);
    }
}
