package io.github.luigeneric.templates.utils;

public enum ShipConsumableOption
{
    Undefined(0),
    Using(1),
    NotUsing(2),
    Optional(3);

    public final byte value;

    ShipConsumableOption(int value)
    {
        this.value = (byte) value;
    }
}
