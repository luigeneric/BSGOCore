package io.github.luigeneric.core.spaceentities.statsinfo.stats;

public class CombatInfo
{
    private long lastCombatTime;
    private boolean isInCombat;

    public CombatInfo(final long lastCombatTime, final boolean isInCombat)
    {
        this.lastCombatTime = lastCombatTime;
        this.isInCombat = isInCombat;
    }
    public CombatInfo()
    {
        this(0, false);
    }

    public boolean isCombatStatusChanged(final long combatTimerSeconds, final long currentEpochMilliseconds, final boolean hasMissile)
    {
        final long diff = currentEpochMilliseconds - (this.lastCombatTime + combatTimerSeconds * 1000);
        final boolean isInCombatNow = this.lastCombatTime != 0 && (diff < 0 || hasMissile);
        final boolean combatStatusChanged = this.isInCombat != isInCombatNow;
        this.isInCombat = isInCombatNow;

        return combatStatusChanged;
    }
    public void setLastCombatTime(final long timeStamp)
    {
        this.lastCombatTime = timeStamp;
    }

    public boolean isInCombat()
    {
        return isInCombat;
    }

    @Override
    public String toString()
    {
        return "CombatInfo{" +
                "lastCombatTime=" + lastCombatTime +
                ", isInCombat=" + isInCombat +
                '}';
    }
}
