package io.github.luigeneric.core.gameplayalgorithms;

/**
 * Just ignore armor entirely
 */
public class ArmorAlgorithmV0 implements IArmorAlgorithm
{
    @Override
    public float getMultiplicator(float armor, float armorPiercing)
    {
        return 1;
    }
}
