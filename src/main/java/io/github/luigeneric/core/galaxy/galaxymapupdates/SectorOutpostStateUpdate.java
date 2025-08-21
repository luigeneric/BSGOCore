package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorOutpostStateUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final float delta;


    public SectorOutpostStateUpdate(Faction faction, long sectorID, final float delta)
    {
        super(GalaxyUpdateType.SectorOutpostState, faction);
        this.sectorID = sectorID;
        this.delta = delta;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(this.faction.value);
        bw.writeSingle(this.delta);
    }
}
