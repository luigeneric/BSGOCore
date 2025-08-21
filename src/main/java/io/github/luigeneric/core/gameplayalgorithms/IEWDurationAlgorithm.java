package io.github.luigeneric.core.gameplayalgorithms;

public interface IEWDurationAlgorithm
{
    /**
     * Calculates the hack duration based on own emitter and enemy firewall
     * @param baseDuration the duration of the item
     * @param emitterRate of the the ship casting the hack
     * @param enemyFirewall of the ship the hack should be casted on
     * @return the time in seconds (because baseDuration is in seconds as well)
     */
    float getHackDuration(final float baseDuration, final float emitterRate, final float enemyFirewall);
}
