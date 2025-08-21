package io.github.luigeneric.templates.startupconfig;

public interface StarterParams
{
    long startTylium();

    long startCubits();

    long startTitanium();

    long startToken();

    int dailyTokenCap();

    boolean testingMode();

    default boolean isLive()
    {
        return !testingMode();
    }
}
