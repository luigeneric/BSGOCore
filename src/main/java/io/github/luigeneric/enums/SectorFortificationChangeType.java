package io.github.luigeneric.enums;

public enum SectorFortificationChangeType
{
    Equal,
    Increment,
    Decrement;

    public static final int SIZE = Integer.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static SectorFortificationChangeType forValue(final byte value)
    {
        final SectorFortificationChangeType[] arr = values();
        if (value > arr.length || value < arr.length)
            return null;
        return values()[value];
    }
}
