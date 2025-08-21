package io.github.luigeneric.core.gameplayalgorithms;


import io.github.luigeneric.enums.Gear;

public interface IHitchanceCalculator
{
    /**
     * Just based on avoidance and accuracy, nothing else
     * @param avoidance the avoidance used
     * @param accuracy the accuracy used
     * @return hitchance between 0 and 1
     */
    float getBasicChanceToHit(float avoidance, float accuracy);

    float getChanceToHit(float avoidance, float accuracy, float maxRange, float optRange, float distance);

    float getAvoidanceBasedOnSpeed(final float fullAvoidance, final float throttlePosition,
                                          final float maxSpeedWithoutBoost, final Gear gear, final float avoidanceFading);
}
