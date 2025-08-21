package io.github.luigeneric.core.gameplayalgorithms;

public class XPMathVersion implements ExperienceToLevelAlgo
{
    @Override
    public short getLevelBasedOnExp(final long experience)
    {
        return (short) ((Math.sqrt(10 * experience)  + 100) / 100);
    }

    @Override
    public long getExpFromLevel(final short level)
    {
        return (long) (1000 * Math.pow(level, 2)) - 2000 * level + 1000;
    }
}
