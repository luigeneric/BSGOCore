package io.github.luigeneric.core.gameplayalgorithms;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;

@Dependent
@Default
public class MathVersionOriginal implements ExperienceToLevelAlgo
{

    public MathVersionOriginal()
    {}

    @Override
    public short getLevelBasedOnExp(final long experience)
    {
        return (short) Math.min(255, Math.max(1, Math.sqrt((double) experience / 1000) + 1));
    }

    @Override
    public long getExpFromLevel(final short level)
    {
        if (level < 1) throw new IllegalArgumentException("Level cannot be lower than 1");

        return (long) Math.pow((level-1), 2) * 1000;
    }
}
