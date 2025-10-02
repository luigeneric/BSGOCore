package io.github.luigeneric.core.sector.management.damage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DamageCalculatorTest
{

    @ParameterizedTest
    @CsvSource(
            value = {
                    "200, 400, 600, 1, 0", // max distance
                    "200, 400, 0, 1, 1", // min distance
                    "200, 400, 400, 1, 0.5" // distance is half, therefore, should be half damage
            }
    )
    void calAoEMult(float aoeInner, float aoeOuter, float distance, float aoeDropoffIndex, float expectedMult)
    {
        var multResult = DamageCalculator.calAoEMult(aoeInner, aoeOuter, distance, aoeDropoffIndex);
        assertEquals(expectedMult, multResult);
    }
}