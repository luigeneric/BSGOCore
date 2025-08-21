package io.github.luigeneric.templates.utils;

public enum AbilityActionType
{
    None,
    FireMissle,
    FireCannon,
    DropFlare,
    Buff,
    RestoreBuff,
    ResourceScan,
    Debuff,
    FireMining,
    Flak,
    PointDefence,
    DispellVirus,
    Follow,
    ManeuverFlip,
    Slide,
    ActivatePaintTheTarget,
    FollowFriend,
    ActivateJumpTargetTransponder,
    ToggleStealth,
    FireTorpedo,
    ToggleSystem,
    FireLightMissile,
    FireHeavyMissile,
    FireShotgun,
    FireKillCannon,
    FireMachineGun,
    Fortify,
    DevBuff,
    ShortCircuit,
    DropAntiStealthMine,
    DeflectMissile;

    public byte value()
    {
        return (byte) this.ordinal();
    }
}
