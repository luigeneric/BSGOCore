package io.github.luigeneric;

import io.github.luigeneric.utils.BgoRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class BgoRandomTest
{
    @Test
    void testRandomChance()
    {
        final BgoRandom bgoRandom = new BgoRandom();
        final Map<Boolean, Integer> counterOnRollChance = new HashMap<>();
        counterOnRollChance.put(true, 0);
        counterOnRollChance.put(false, 0);

        final int TIMES = 1_000_000;
        for (int i = 0; i < TIMES; i++)
        {
            final boolean rollResult = bgoRandom.rollChance(0.05f);
            final int num = counterOnRollChance.get(rollResult);
            counterOnRollChance.put(rollResult, num + 1);
        }

        float trueNum = counterOnRollChance.get(true);
        float falseNum = counterOnRollChance.get(false);

        float pTrue = (trueNum / TIMES) * 100;
        float pFalse = (falseNum / TIMES) * 100;

        // allow 0.05% deviation
        Assertions.assertEquals(5f, pTrue, 0.05f);
        Assertions.assertEquals(95f, pFalse, 0.05f);
    }
}
