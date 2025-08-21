package io.github.luigeneric.core.gameplayalgorithms;

public interface ICritchanceAlgorithm
{
    float getCritChance(final float critAttack, final float critDefense);
    float getOverallCriticalMultiplier(boolean isCrit);
}
