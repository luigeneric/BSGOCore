package io.github.luigeneric.core.gameplayalgorithms;

public interface IFlareChanceAlgorithm
{
    /**
     * Calculates the chance for first flare to hit inside the range
     * @param distance
     * @param flareRange
     * @return the chance for first flare to hit in [0.1, 0.95]
     * @throws IllegalArgumentException if either the distance is greater than flareRange or the flareRange is 0
     */
    float getChanceForFlareSuccess(final float distance, final float flareRange);
}
