package io.github.luigeneric.templates.utils;

public enum ConsumableEffectType
{
    None,
    DamageKinetic,
    DamageExplosion,
    DamageNuclear,
    DamageShrapnel,
    DamageHighVelocity,
    DamageEmp,
    Heal,
    Recharge,
    Scan,
    Buff;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }
}
