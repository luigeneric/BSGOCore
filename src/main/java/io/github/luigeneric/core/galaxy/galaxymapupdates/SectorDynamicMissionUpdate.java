package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorDynamicMissionUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final short dynamicMissionCount;

    public SectorDynamicMissionUpdate(final Faction faction, long sectorID, short dynamicMissionCount)
    {
        super(GalaxyUpdateType.SectorDynamicMissions, faction);
        this.sectorID = sectorID;
        this.dynamicMissionCount = dynamicMissionCount;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(this.faction.value);
        bw.writeByte((byte) this.dynamicMissionCount);
    }
}
