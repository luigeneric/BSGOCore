package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class GalaxyRcpUpdate extends GalaxyMapUpdate
{
    private final float rcp;

    public GalaxyRcpUpdate(Faction faction, final float newRCP)
    {
        super(GalaxyUpdateType.Rcp, faction);
        this.rcp = newRCP;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(this.faction.value);
        bw.writeSingle(this.rcp);
    }
}
