package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

import java.time.LocalDateTime;

public class ConquestLocationUpdate extends GalaxyMapUpdate
{
    private final int sectorID;
    private final LocalDateTime conquestExpireDate;

    public ConquestLocationUpdate(Faction faction, final int sectorID, final LocalDateTime conquestExpireDate)
    {
        super(GalaxyUpdateType.ConquestLocation, faction);
        this.sectorID = sectorID;
        this.conquestExpireDate = conquestExpireDate;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte((byte) this.faction.value);
        bw.writeInt32(this.sectorID);
        bw.writeDateTime(this.conquestExpireDate);
    }
}
