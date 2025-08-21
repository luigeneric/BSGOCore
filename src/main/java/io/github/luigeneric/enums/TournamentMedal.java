package io.github.luigeneric.enums;

public enum TournamentMedal
{
    None,
    GoldMedal,
    SilverMedal;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static TournamentMedal forValue(byte value)
    {
        return values()[value];
    }
}
