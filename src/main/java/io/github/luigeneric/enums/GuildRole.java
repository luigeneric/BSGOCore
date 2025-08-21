package io.github.luigeneric.enums;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum GuildRole implements IProtocolWrite
{
    None,
    Recruit,
    Pilot,
    SeniorPilot,
    FlightLeader,
    GroupLeader,
    Leader;

    public static final int SIZE = Byte.SIZE;

    public final byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static GuildRole forValue(byte value)
    {
        return values()[value];
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(getValue());
    }
}

