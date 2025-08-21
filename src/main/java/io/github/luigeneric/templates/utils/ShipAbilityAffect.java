package io.github.luigeneric.templates.utils;

public enum ShipAbilityAffect
{
    Selected(0),
    Ignore(1),
    Area(2),
    MultiWeaponTarget(3);

    public final byte value;
    ShipAbilityAffect(int value)
    {
        this.value = (byte) value;
    }
}
