package io.github.luigeneric.enums;

public enum WeaponFxType
{
    Undefined,
    Gun,
    MissileLauncher,
    PointDefence,
    Flak,
    Shrapnel,
    SpotFlak,
    AoEFlak,
    MachineGun,
    Flechete,
    Railgun;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static WeaponFxType forValue(byte value)
    {
        return values()[value];
    }
}
