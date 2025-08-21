package io.github.luigeneric.enums;

public enum GameLocation
{
    Unknown,
    Space,
    Room,
    Story,
    Disconnect,
    Arena,
    BattleSpace,
    Tournament,
    Tutorial,
    Teaser,
    Avatar,
    Starter,
    Zone;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static GameLocation forValue(byte value)
    {
        return values()[value];
    }
}
