package io.github.luigeneric.templates.utils;

public enum ShipSlotType
{
    undefined,
    computer,
    engine,
    hull,
    weapon,
    ship_paint,
    avionics,
    launcher,
    defensive_weapon,
    gun,
    role,
    special_weapon;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }
}
