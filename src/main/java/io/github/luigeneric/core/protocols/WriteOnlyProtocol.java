package io.github.luigeneric.core.protocols;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.Objects;

public abstract class WriteOnlyProtocol
{
    protected final ProtocolID protocolID;

    public WriteOnlyProtocol(final ProtocolID protocolID)
    {
        this.protocolID = protocolID;
    }

    protected BgoProtocolWriter newMessage()
    {
        final BgoProtocolWriter bw = new BgoProtocolWriter();
        bw.writeByte(this.protocolID.value);
        return bw;
    }

    public ProtocolID getProtocolID()
    {
        return this.protocolID;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WriteOnlyProtocol that = (WriteOnlyProtocol) o;
        return protocolID == that.protocolID;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(protocolID);
    }
}
