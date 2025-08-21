package io.github.luigeneric.core.gameplayalgorithms;

import io.github.luigeneric.linearalgebra.utility.Mathf;

/**
 * Old armor algorithm (presumably used in the game back then)
 */
public class ArmorAlgorithmV1 implements IArmorAlgorithm
{

    @Override
    public float getMultiplicator(final float armor, final float armorPiercing)
    {
        final float armorDiff = Mathf.clampSafe(armor - armorPiercing, 0f, 99.9f);

        // [001, 1] in R
        return (100f - armorDiff) * 0.01f;
    }
}
