package io.github.luigeneric.core.gameplayalgorithms;

public interface ExperienceToLevelAlgo
{
    short getLevelBasedOnExp(long experience);
    long getExpFromLevel(short level);
}
