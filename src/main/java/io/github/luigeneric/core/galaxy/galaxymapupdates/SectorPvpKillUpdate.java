package io.github.luigeneric.core.galaxy.galaxymapupdates;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorPvpKillUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final int killsCount;

    public SectorPvpKillUpdate(Faction faction, long sectorID, int killsCount)
    {
        super(GalaxyUpdateType.SectorPvPKills, faction);
        this.sectorID = sectorID;
        this.killsCount = killsCount;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(this.faction.value);
        bw.writeInt32(this.killsCount);
    }
}
