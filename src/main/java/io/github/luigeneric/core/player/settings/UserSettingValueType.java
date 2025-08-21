package io.github.luigeneric.core.player.settings;

public enum UserSettingValueType
{
    Unknown,
    Float,
    Boolean,
    Integer,
    Float2,
    HelpScreenType,
    Byte;

    public static final int SIZE = java.lang.Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static UserSettingValueType forValue(byte value)
    {
        return values()[value];
    }
}
