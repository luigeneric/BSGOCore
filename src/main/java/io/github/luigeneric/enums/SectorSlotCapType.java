package io.github.luigeneric.enums;

public enum SectorSlotCapType
{
    Colonial,
    Cylon,
    Total;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static SectorSlotCapType forValue(byte value)
    {
        return values()[value];
    }
}
