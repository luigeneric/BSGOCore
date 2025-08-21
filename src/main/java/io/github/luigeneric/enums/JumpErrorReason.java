package io.github.luigeneric.enums;

public enum JumpErrorReason
{
    Unknown,
    LeaderLeft,
    ChildLeft,
    ParentLeft,
    Substitute,
    SectorClosed,
    ShipClosed,
    SectorBusy,
    ShipBusy,
    LostJumpTarget,
    SlotsExpired,
    Faction,
    Restricted,
    AnchoredState,
    FtlRange,
    WrongSector,
    NotEnoughTylium,
    WrongShip,
    WrongParty,
    LevelTooLow,
    LevelTooHigh,
    Tier,
    Busy,
    Closed,
    JumpCost,
    Queued,
    PlayerCanceled,
    SectorOverflow,
    ShipOverflow;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static JumpErrorReason forValue(byte value)
    {
        return values()[value];
    }
}

