package io.github.luigeneric.enums;

public enum AssistMedal
{
    None,
    GoldMedal,
    SilverMedal,
    BronzeMedal;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static AssistMedal forValue(byte value)
    {
        return values()[value];
    }
}

