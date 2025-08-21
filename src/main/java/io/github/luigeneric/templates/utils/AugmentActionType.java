package io.github.luigeneric.templates.utils;

public enum AugmentActionType
{
    None (0),
    SkillTime (1), //reduces skill time
    Lock (2),
    @Deprecated
    SwitchFaction(3),
    Teleport(4), //ftl overcharge
    LootItem(5); //not identified objects

    public final byte value;
    AugmentActionType(int value)
    {
        this.value = (byte) value;
    }
}