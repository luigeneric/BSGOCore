package io.github.luigeneric.core.gameplayalgorithms;

public interface IArmorAlgorithm
{
    /**
     * Calculates the damage multiplicator for the armor values
     * @param armor armor value of the ship to shoot at
     * @param armorPiercing armor piercing of the gun to shoot with
     * @return the coefficient for dmg multiplication
     */
    float getMultiplicator(final float armor, final float armorPiercing);
}
