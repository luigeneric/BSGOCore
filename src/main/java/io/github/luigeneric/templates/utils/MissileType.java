package io.github.luigeneric.templates.utils;

public enum MissileType
{
    Normal(0),
    Nuke(1),
    Torpedo(2);

    public final byte value;
    MissileType(int value)
    {
        this.value = (byte) value;
    }
}