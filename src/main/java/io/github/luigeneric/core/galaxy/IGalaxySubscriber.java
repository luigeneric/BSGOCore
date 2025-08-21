package io.github.luigeneric.core.galaxy;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.Faction;

public interface IGalaxySubscriber
{
    long getID();
    Faction getFaction();
    void mapUpdateReceived(final BgoProtocolWriter galaxyMapUpdatesWriter);
}
