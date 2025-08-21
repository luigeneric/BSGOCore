package io.github.luigeneric.templates.npcbehaviour;

import java.util.Objects;

/**
 *
 **/
public final class NpcBehaviourTemplate
{
    private final long id;
    private final float autoAggroDistance;
    private final float maximumAggroDistance;
    private final float lifeTimeSeconds;
    private final boolean jumpOutIfInCombat;
    private final float speedZeroDistance;

    /**
     * @param autoAggroDistance distance from when the npc starts attacking without hestiation
     * @param maximumAggroDistance distance a npc will hold you into it's target, if outside you will be removed from the ObjectDamageHistory
     * @param lifeTimeSeconds      the time in seconds a npc can be inside a sector until it jumps out
     * @param jumpOutIfInCombat    if in combat the npc will still jump out, if false the npc should not jump out until combat is over
     * @param speedZeroDistance    the distance from the npc to the target the npc will be stop at
     */
    public NpcBehaviourTemplate(long id, float autoAggroDistance, float maximumAggroDistance, float lifeTimeSeconds,
                                boolean jumpOutIfInCombat, float speedZeroDistance)
    {
        this.id = id;
        this.autoAggroDistance = autoAggroDistance;
        this.maximumAggroDistance = maximumAggroDistance;
        this.lifeTimeSeconds = lifeTimeSeconds;
        this.jumpOutIfInCombat = jumpOutIfInCombat;
        this.speedZeroDistance = speedZeroDistance;
    }

    public long id()
    {
        return id;
    }

    public float autoAggroDistance()
    {
        return autoAggroDistance;
    }

    public float maximumAggroDistance()
    {
        return maximumAggroDistance;
    }

    public float lifeTimeSeconds()
    {
        return lifeTimeSeconds;
    }

    public boolean jumpOutIfInCombat()
    {
        return jumpOutIfInCombat;
    }

    public float speedZeroDistance()
    {
        return speedZeroDistance;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NpcBehaviourTemplate) obj;
        return this.id == that.id &&
                Float.floatToIntBits(this.autoAggroDistance) == Float.floatToIntBits(that.autoAggroDistance) &&
                Float.floatToIntBits(this.maximumAggroDistance) == Float.floatToIntBits(that.maximumAggroDistance) &&
                Float.floatToIntBits(this.lifeTimeSeconds) == Float.floatToIntBits(that.lifeTimeSeconds) &&
                this.jumpOutIfInCombat == that.jumpOutIfInCombat &&
                Float.floatToIntBits(this.speedZeroDistance) == Float.floatToIntBits(that.speedZeroDistance);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, autoAggroDistance, maximumAggroDistance, lifeTimeSeconds, jumpOutIfInCombat, speedZeroDistance);
    }

    @Override
    public String toString()
    {
        return "NpcBehaviourTemplate[" +
                "id=" + id + ", " +
                "autoAggroDistance=" + autoAggroDistance + ", " +
                "maximumAggroDistance=" + maximumAggroDistance + ", " +
                "lifeTimeSeconds=" + lifeTimeSeconds + ", " +
                "jumpOutIfInCombat=" + jumpOutIfInCombat + ", " +
                "speedZeroDistance=" + speedZeroDistance + ']';
    }

}
