package io.github.luigeneric.enums;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum SectorEventTaskType implements IProtocolWrite
{
    Unknown,
    Protect;

    //public static final int SIZE = java.lang.Integer.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static SectorEventTaskType forValue(int value)
    {
        return values()[value];
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(getValue());
    }
}
