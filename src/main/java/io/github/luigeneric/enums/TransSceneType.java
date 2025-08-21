package io.github.luigeneric.enums;

public enum TransSceneType
{
    None,
    Die,
    Undock,
    Ftl,
    Hangar,
    CIC,
    Recroom,
    Outpost,
    Minigfacility,
    FirstStory,
    Dock,
    Arena,
    Teaser,
    Battlespace,
    Tournament;

    public byte getValue()
    {
        return (byte)this.ordinal();
    }
}
