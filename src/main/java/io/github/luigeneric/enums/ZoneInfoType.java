package io.github.luigeneric.enums;

public enum ZoneInfoType
{
    None(0),
    Scavenger(1);

    public final byte value;

    ZoneInfoType(int i)
    {
        this.value = (byte) i;
    }
}
