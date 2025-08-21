package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorBeaconStateUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final float state;

    public SectorBeaconStateUpdate(Faction faction, long sectorID, float state)
    {
        super(GalaxyUpdateType.SectorBeaconState, faction);
        this.sectorID = sectorID;
        this.state = state;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(this.faction.value);
        bw.writeSingle(this.state);
    }
}
