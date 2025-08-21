package io.github.luigeneric.enums;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum ZonePlugin implements IProtocolWrite
{
    FactionBoard(1),
    ScoreBoard(2),
    FfaScoring(3);

    public final byte value;

    ZonePlugin(int i)
    {
        this.value = (byte) i;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(this.value);
    }
}