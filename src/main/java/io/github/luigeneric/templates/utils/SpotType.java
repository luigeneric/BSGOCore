package io.github.luigeneric.templates.utils;

public enum SpotType
{
    Weapon,
    Sticker,
    Mining,
    Door;

    public byte getValue()
    {
        return (byte) (this.ordinal() + 1);
    }

}
