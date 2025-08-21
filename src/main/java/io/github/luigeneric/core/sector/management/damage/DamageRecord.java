package io.github.luigeneric.core.sector.management.damage;


import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.Objects;

public record DamageRecord(SpaceObject from, SpaceObject to, float damage, boolean isCritical, long timeStamp, boolean isKillShot)
{
    public DamageRecord
    {
        Objects.requireNonNull(from, "From cannot be null");
        Objects.requireNonNull(to, "To cannot be null");
        if (damage < 0) throw new IllegalArgumentException("Damage dealt cannot be less than 0!");
    }
    public DamageRecord(final SpaceObject from, final SpaceObject to, final float damage, final boolean isCritical, final long timeStamp)
    {
        this(from, to, damage, isCritical, timeStamp, false);
    }
    public DamageRecord(final DamageRecord damageRecord, final float damage, final boolean wasKillShot)
    {
        this(damageRecord.from, damageRecord.to, damage, damageRecord.isCritical, damageRecord.timeStamp, wasKillShot);
    }
}
