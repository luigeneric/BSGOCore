package io.github.luigeneric.enums;

public enum ChangeVisibilityReason
{
    Default,
    Jump,
    Death,
    Anchor;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static ChangeVisibilityReason forValue(byte value)
    {
        return values()[value];
    }
}

