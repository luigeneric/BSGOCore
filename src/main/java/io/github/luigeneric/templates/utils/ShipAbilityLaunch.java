package io.github.luigeneric.templates.utils;

public enum ShipAbilityLaunch
{
    None(0),
    Auto(1),
    Manual(2);

    public final byte value;

    ShipAbilityLaunch(int value)
    {
        this.value = (byte) value;
    }
}
