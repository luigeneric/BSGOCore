package io.github.luigeneric.enums;

public enum PartyMemberFtlState
{
    Ignore,
    Wait,
    Anchored,
    Ready;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static PartyMemberFtlState forValue(byte value)
    {
        return values()[value];
    }
}
