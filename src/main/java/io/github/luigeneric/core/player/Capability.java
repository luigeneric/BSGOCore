package io.github.luigeneric.core.player;

public enum Capability
{
    Gear,
    PPRecovery,
    Undock,
    Loot,
    Cast,
    PlayWof,
    Repair,
    SelectShip,
    MainShop,
    Ftl,
    HPRecovery,
    VPRecovery;

    public static final int SIZE = Integer.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static Capability forValue(final int value)
    {
        return values()[value];
    }
}
