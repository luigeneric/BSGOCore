package io.github.luigeneric.enums;

public enum JumpErrorSeverity
{
    Info,
    Notify,
    Error;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static JumpErrorSeverity forValue(byte value)
    {
        return values()[value];
    }
}