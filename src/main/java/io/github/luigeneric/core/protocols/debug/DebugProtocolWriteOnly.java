package io.github.luigeneric.core.protocols.debug;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

public class DebugProtocolWriteOnly extends WriteOnlyProtocol
{
    public DebugProtocolWriteOnly()
    {
        super(ProtocolID.Debug);
    }

    public BgoProtocolWriter writeUpdateRoles(final long newRoleBits)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.UpdateRoles.intValue);
        bw.writeUInt32(newRoleBits);

        return bw;
    }
    public BgoProtocolWriter writeProcessState(final String state)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ProcessState.intValue);
        bw.writeString(state);

        return bw;
    }

    public BgoProtocolWriter writeMessage(final CharSequence msg)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Message.getValue());
        bw.writeString(msg.toString());
        return bw;
    }

    public BgoProtocolWriter writeCommand(final CharSequence msg)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Command.intValue);
        bw.writeString(msg.toString());
        return bw;
    }
}
