package io.github.luigeneric.core.gameplayalgorithms;


import io.github.luigeneric.linearalgebra.utility.Mathf;

/**
 * provided by a user, never used
 */
public class ArmorAlgorithmV2 implements IArmorAlgorithm
{
    @Override
    public float getMultiplicator(float armor, float armorPiercing)
    {
        return Mathf.clamp01(100f / (100f + (armor - armorPiercing)));
    }
}
