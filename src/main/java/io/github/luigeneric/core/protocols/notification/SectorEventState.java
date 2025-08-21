package io.github.luigeneric.core.protocols.notification;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum SectorEventState implements IProtocolWrite
{
    Inactive,
    Active,
    Success,
    Failed;

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte((byte) this.ordinal());
    }
}
