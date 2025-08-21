package io.github.luigeneric.core.gameplayalgorithms;

public class FlareChanceAlgorithmV1 implements IFlareChanceAlgorithm
{
    @Override
    public float getChanceForFlareSuccess(final float distance, final float flareRange)
    {
        if (flareRange == 0)
            throw new IllegalArgumentException("Flare range is zero!");


        final float diff = flareRange - distance;
        if (diff < 0)
            throw new IllegalArgumentException("distance cannot be higher than flareRange!");

        //0 <=> flareRange = distance <=> chance is 0.1f
        //1000 <=> distance = 0 <=> chance is 0.95f

        // y2 = 0.95
        // y1 = 0.1

        //x1 = 0
        //x2 = 1000

        final float m = (0.95f - 0.1f) / (flareRange);
        final float b = 0.1f + m * 0f;
        return m * diff + b;
    }
}
