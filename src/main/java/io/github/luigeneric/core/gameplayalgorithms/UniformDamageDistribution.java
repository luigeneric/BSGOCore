package io.github.luigeneric.core.gameplayalgorithms;

import io.github.luigeneric.utils.BgoRandom;

public class UniformDamageDistribution implements DamageCalcStrategy
{
    @Override
    public float getDamageRoll(final BgoRandom bgoRandom, final float min, final float max)
    {
        return bgoRandom.getRndBetween(min, max);
    }
}
