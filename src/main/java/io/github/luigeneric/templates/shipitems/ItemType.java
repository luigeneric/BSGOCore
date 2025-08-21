package io.github.luigeneric.templates.shipitems;

public enum ItemType
{
    None,
    System,
    Countable,
    Starter,
    Ship;

    public final byte getValue()
    {
        return (byte) this.ordinal();
    }
}
