package io.github.luigeneric.core.protocols.player;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum UnanchorReason implements IProtocolWrite
{
    Default(0),
    Timeout(1),
    Killed(2);

    public final byte value;
    UnanchorReason(int i)
    {
        this.value = (byte) i;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(this.value);
    }
}
