package io.github.luigeneric.core.gameplayalgorithms;

import io.github.luigeneric.linearalgebra.utility.Mathf;

public class CritchanceAlgorithmV1 implements ICritchanceAlgorithm
{
    /**
     * base chance without crit-attack or crit-defense
     */
    private final float baseCritChance;
    /**
     * how much the difference in crit-attack and crit-defense is "worth"; higher is more
     */
    private final float multScalar;
    private final float overallCriticalMultiplier;

    public CritchanceAlgorithmV1(final float baseCritChance, final float multScalar, final float overallCriticalMultiplier)
    {
        this.baseCritChance = baseCritChance;
        this.multScalar = multScalar;
        this.overallCriticalMultiplier = overallCriticalMultiplier;
    }
    public CritchanceAlgorithmV1()
    {
        this(5.f, 0.15f, 2f);
    }


    @Override
    public float getCritChance(float critAttack, float critDefense)
    {
        final float baseCritChance = (this.baseCritChance + (this.multScalar * (critAttack - critDefense))) * 0.01f;
        // cap between min=0% and max=10% orig, cap is now open
        // current: just prevent negative critchance
        return Mathf.clamp01(baseCritChance);
    }

    @Override
    public float getOverallCriticalMultiplier(final boolean isCrit)
    {
        return isCrit ? overallCriticalMultiplier : 1f;
    }
}
