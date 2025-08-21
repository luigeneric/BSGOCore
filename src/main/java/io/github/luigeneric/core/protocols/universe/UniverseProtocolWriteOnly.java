package io.github.luigeneric.core.protocols.universe;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.galaxy.galaxymapupdates.GalaxyMapUpdate;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

import java.util.List;

public class UniverseProtocolWriteOnly extends WriteOnlyProtocol
{
    public UniverseProtocolWriteOnly()
    {
        super(ProtocolID.Universe);
    }

    public BgoProtocolWriter writeUpdate(final GalaxyMapUpdate galaxyMapUpdate)
    {
        return writeUpdates(List.of(galaxyMapUpdate));
    }
    public BgoProtocolWriter writeUpdates(final List<GalaxyMapUpdate> galaxyMapUpdates)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeDesc(ServerMessage.Update);
        bw.writeDescCollection(galaxyMapUpdates);

        return bw;
    }
}
