package io.github.luigeneric.core.gameplayalgorithms;

import io.github.luigeneric.utils.BgoRandom;


public class DamageGaussianDistribution implements DamageCalcStrategy
{
    @Override
    public float getDamageRoll(final BgoRandom bgoRandom, float min, float max)
    {
        if (min > max)
        {
            return getDamageRoll(bgoRandom, max, min);
        }

        final float mean = (min + max) / 2.0f;

        // (3σ for min max)
        final float stdDev = (max - min) / 6.0f;

        double gaussian = bgoRandom.nextGaussian(mean, stdDev);
        int attempts = 0;
        int maxAttempts = 10; // limit maxAttempts

        while ((gaussian < min || gaussian > max) && attempts < maxAttempts) {
            gaussian = bgoRandom.nextGaussian(mean, stdDev);
            attempts++;
        }

        if (gaussian < min) {
            gaussian = min;
        } else if (gaussian > max) {
            gaussian = max;
        }

        return (float) gaussian;
    }
}