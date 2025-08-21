package io.github.luigeneric.templates.utils;

public enum StatView
{
    MinRange,
    MaxRange,
    OptimalRange,
    Duration,
    Cooldown,
    BuffCost,
    Durability,
    Target,
    DMGLow,
    DMGHigh,
    Accuracy,
    CriticalOffense,
    Angle,
    Mining,
    FlareRange,
    RestoreBuff,
    RemoteBuffMultiply,
    StaticBuff,
    RestorePowerBuff,
    DrainLow,
    DrainHigh,
    ArmorPiercing,
    ArmorValue,
    HPRecovery,
    PPRecovery,
    Speed,
    TurnSpeed,
    TurnAcceleration,
    InertiaCompensation,
    MultiplyBuff,
    RemoteBuffAdd,
    AoERadius,
    BuffCostPerSecond,
    ToggleSystemAdd,
    ToggleSystemMultiply,
    HP,
    LifeTime,
    RestoreVitalBuff;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }
}
