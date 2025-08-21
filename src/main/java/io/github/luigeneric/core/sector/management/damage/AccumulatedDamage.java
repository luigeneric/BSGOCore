package io.github.luigeneric.core.sector.management.damage;

import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.Objects;

public class AccumulatedDamage implements Comparable<AccumulatedDamage>
{
    private final SpaceObject dealer;
    private final long firstTime;
    private long lastTime;
    private float accumulatedDamage;
    private float dmgBasedOnBuffs;
    private float dmgBasedOnDebuffs;
    private boolean killShot;

    public AccumulatedDamage(final SpaceObject dealer, final long firstTime, final float initialDamage, final boolean isKillShot)
    {
        this.dealer = dealer;
        this.firstTime = firstTime;
        this.lastTime = firstTime;
        this.accumulatedDamage = initialDamage;
        this.killShot = isKillShot;
        this.dmgBasedOnBuffs = 0;
        this.dmgBasedOnDebuffs = 0;
    }

    public void update(final long timeStamp, final float damageDone, final boolean wasKillShot)
    {
        if (damageDone < 0) throw new IllegalArgumentException("DamageDone in history cannot be less than 0!");
        this.lastTime = timeStamp;
        this.accumulatedDamage += damageDone;
        if (wasKillShot)
        {
            this.killShot = true;
        }
    }
    public void updateModifierDmg(final SpaceObject dealer, final float dmg, final boolean isBuff)
    {
        //do not add dmg if it's a modifier of yourself
        if (this.dealer.equals(dealer))
            return;

        if (isBuff)
        {
            dmgBasedOnBuffs += dmg;
        }
        else
        {
            dmgBasedOnDebuffs += dmg;
        }
    }

    public long getFirstTime()
    {
        return firstTime;
    }

    public long getLastTime()
    {
        return lastTime;
    }

    public float getAccumulatedDamage()
    {
        return accumulatedDamage;
    }

    public SpaceObject getDealer()
    {
        return dealer;
    }

    @Override
    public int compareTo(final AccumulatedDamage o)
    {
        return -Float.compare(this.accumulatedDamage, o.accumulatedDamage);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccumulatedDamage that = (AccumulatedDamage) o;

        return Objects.equals(dealer, that.dealer);
    }

    @Override
    public int hashCode()
    {
        return dealer != null ? dealer.hashCode() : 0;
    }

    public boolean isKillShot()
    {
        return killShot;
    }


    public float getDmgBasedOnBuffs()
    {
        return dmgBasedOnBuffs;
    }

    public float getDmgBasedOnDebuffs()
    {
        return dmgBasedOnDebuffs;
    }
}
