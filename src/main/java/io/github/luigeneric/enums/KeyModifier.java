package io.github.luigeneric.enums;

public enum KeyModifier
{
    None,
    Shift,
    Control;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static KeyModifier forValue(byte value)
    {
        return values()[value];
    }
}
