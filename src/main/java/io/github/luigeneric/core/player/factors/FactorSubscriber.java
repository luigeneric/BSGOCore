package io.github.luigeneric.core.player.factors;

public interface FactorSubscriber
{
    void notifyFactorStarted(final long userID, final Factor factor);
}
