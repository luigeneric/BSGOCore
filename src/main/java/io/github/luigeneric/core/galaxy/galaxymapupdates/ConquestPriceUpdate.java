package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class ConquestPriceUpdate extends GalaxyMapUpdate
{
    private final long price;

    public ConquestPriceUpdate(Faction faction, long price)
    {
        super(GalaxyUpdateType.ConquestPrice, faction);
        this.price = price;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(this.faction.value);
        bw.writeUInt32(this.price);
    }
}
