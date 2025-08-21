package io.github.luigeneric.core.gameplayalgorithms;


import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitchanceBasedOnThrottle implements IHitchanceCalculator
{
    private static final Logger log = LoggerFactory.getLogger(HitchanceBasedOnThrottle.class);
    private final float maxHitChance;
    private final float minHitChance;
    private final float minHitChanceOutsideOpt;

    public HitchanceBasedOnThrottle(final float maxHitChance, final float minHitChance, final float minHitChanceOutsideOpt)
    {
        this.maxHitChance = maxHitChance;
        this.minHitChance = minHitChance;
        this.minHitChanceOutsideOpt = minHitChanceOutsideOpt;
    }
    public HitchanceBasedOnThrottle()
    {
        this(0.95f, 0.05f, 0.0001f);
    }

    @Override
    public float getBasicChanceToHit(final float avoidance, final float accuracy)
    {
        final float chance = (67.5f - 0.15f * ( avoidance - accuracy )) * 0.01f;
        //clip between min- and max-chance
        return Mathf.clampSafe(chance, minHitChance, maxHitChance);
    }

    @Override
    public float getChanceToHit(final float avoidance, final float accuracy,
                                final float maxRange, final float optRange,
                                final float distance)
    {
        final float baseChance = getBasicChanceToHit(avoidance, accuracy);
        if (distance <= optRange)
        {
            return baseChance;
        }

        // Linear interpolation outside optimum range
        final float slope = (minHitChanceOutsideOpt - baseChance) / (maxRange - optRange);
        final float intercept = baseChance - slope * optRange;

        return distance * slope + intercept ;
    }

    @Override
    public float getAvoidanceBasedOnSpeed(final float fullAvoidance, final float throttlePosition,
                                          final float maxSpeedWithoutBoost, final Gear gear, final float avoidanceFading)
    {
        if (gear == null)
        {
            log.error("gear was null, this should never happen");
        }

        if (Gear.Boost == gear || avoidanceFading == 0)
            return fullAvoidance;

        if (maxSpeedWithoutBoost == 0)
            return fullAvoidance;

        final float cleanedThrottlePosition = Mathf.min(throttlePosition, maxSpeedWithoutBoost);
        final float throttleQuotient = cleanedThrottlePosition / maxSpeedWithoutBoost;
        final float avoidanceFadingMult = 1f - avoidanceFading;
        final float avoidanceMult = Mathf.max(throttleQuotient, avoidanceFadingMult);
        return fullAvoidance * avoidanceMult;
    }
}
