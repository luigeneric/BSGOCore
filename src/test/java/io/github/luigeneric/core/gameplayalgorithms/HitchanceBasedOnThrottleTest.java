package io.github.luigeneric.core.gameplayalgorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HitchanceBasedOnThrottleTest
{
    IHitchanceCalculator hitchanceCalculator;

    @BeforeEach
    void init()
    {
        hitchanceCalculator = new HitchanceBasedOnThrottle();
    }

    @Test
    void testHitchanceInAvgIsHalf()
    {
        float avoidance = 600;
        float accuracy = 125;
        float maxRange = 2000;
        float optRange = 1500;
        float distance = 1750;

        final float hitChance = hitchanceCalculator.getChanceToHit(avoidance, accuracy, maxRange, optRange, distance);
        assertEquals(0.025f, hitChance, 0.001f);
    }

    @Test
    void testChangeInOptRange()
    {
        float avoidance = 600;
        float accuracy = 125;
        float maxRange = 2000;
        float optRange = 1500;
        float distance = 1500;

        final float hitChance = hitchanceCalculator.getChanceToHit(avoidance, accuracy, maxRange, optRange, distance);
        assertEquals(0.05f, hitChance, 0.001f);
    }

    @Test
    void testHitchancein()
    {
        float avoidance = 600;
        float accuracy = 125;
        float maxRange = 2000;
        float optRange = 1500;
        float distance = 1875;

        final float hitChance = hitchanceCalculator.getChanceToHit(avoidance, accuracy, maxRange, optRange, distance);
        assertEquals(0.0125f, hitChance, 0.001f);
    }
}