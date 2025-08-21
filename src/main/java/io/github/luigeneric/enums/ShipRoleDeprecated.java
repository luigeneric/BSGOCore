package io.github.luigeneric.enums;

public enum ShipRoleDeprecated
{
    None,
    Fighter,
    Defender,
    Command,
    Multi,
    Mothership,
    Carrier,
    Stealth;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static ShipRoleDeprecated forValue(byte value)
    {
        return values()[value];
    }
}

