package io.github.luigeneric.core.protocols.arena;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

public class ArenaProtocolWriteOnly extends WriteOnlyProtocol
{
    public ArenaProtocolWriteOnly()
    {
        super(ProtocolID.Arena);
    }

    public BgoProtocolWriter writeArenaClosed()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ArenaClosed.shortValue);
        return bw;
    }
}
