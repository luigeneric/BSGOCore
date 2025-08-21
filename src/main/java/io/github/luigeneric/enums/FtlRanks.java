package io.github.luigeneric.enums;

public enum FtlRanks
{
    Platinum,
    Gold,
    Silver,
    Bronze;

    public static final int SIZE = Integer.SIZE;

    public int getValue()
    {
        return this.ordinal();
    }

    public static FtlRanks forValue(int value)
    {
        return values()[value];
    }
}
