package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public abstract class GalaxyMapUpdate implements IProtocolWrite
{
    public final GalaxyUpdateType updateType;
    protected final Faction faction;


    public GalaxyMapUpdate(GalaxyUpdateType updateType, Faction faction)
    {
        this.updateType = updateType;
        this.faction = faction;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(this.updateType.getValue());
    }
}
