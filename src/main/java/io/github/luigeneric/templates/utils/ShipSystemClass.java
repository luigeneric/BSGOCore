package io.github.luigeneric.templates.utils;

public enum ShipSystemClass
{
    Standart(1),
    Elite(2);

    public final byte value;

    ShipSystemClass(int value)
    {
        this.value = (byte) value;
    }
}
