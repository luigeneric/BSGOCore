package io.github.luigeneric.core.gameplayalgorithms;

public class FirstFitRecursion implements ExperienceToLevelAlgo
{

    /**
     * If my memory serves me right you started with 2000 and each level the experience needed to reach the new level
     * increased by 1000 so level 1: 0 + 1000, level 2: 1000 + 2000, level 3: 2000 + 3000
     * @param experience
     * @return first value between 0 and 255 (both ends inclusive)
     */
    @Override
    public short getLevelBasedOnExp(long experience)
    {
        short level;
        for (level = 0; level < 255; level++)
        {
            long expForLevel = getExpFromLevel(level);
            if (experience <= expForLevel)
            {
                break;
            }
        }
        return level;
    }

    public long getExpFromLevel(short level)
    {
        if (level == 0)
            return 0;

        return getExpFromLevel((short) (level-1)) + 1000 * level;
    }
}
