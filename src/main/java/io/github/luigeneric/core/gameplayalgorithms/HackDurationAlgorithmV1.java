package io.github.luigeneric.core.gameplayalgorithms;

public class HackDurationAlgorithmV1 implements IEWDurationAlgorithm
{
    /**
     * @implNote Using original formula
     */
    @Override
    public float getHackDuration(final float baseDuration, final float emitterRate, final float enemyFirewall)
    {
        final float emitterMultiplier = (1f + (emitterRate * 0.01f));
        final float firewallMultiplier = (1f / (1f + (enemyFirewall * 0.01f)));

        return baseDuration * emitterMultiplier * firewallMultiplier;
    }
}
