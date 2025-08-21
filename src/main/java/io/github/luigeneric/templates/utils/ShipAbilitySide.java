package io.github.luigeneric.templates.utils;

public enum ShipAbilitySide
{
    Self(1),
    Any(2),
    Neutral(4),
    Friend(8),
    Enemy(16);

    public final byte value;
    ShipAbilitySide(int value)
    {
        this.value = (byte) value;
    }
}
