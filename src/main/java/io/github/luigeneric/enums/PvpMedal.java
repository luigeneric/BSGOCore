package io.github.luigeneric.enums;

public enum PvpMedal
{
    None,
    PvpArena1st,
    PvpArena2nd,
    PvpArena3rd,
    PvpArena4thTo20th,
    PvpArena21stTo100th;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static PvpMedal forValue(byte value)
    {
        return values()[value];
    }
}
