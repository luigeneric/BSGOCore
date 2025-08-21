package io.github.luigeneric.core.gameplayalgorithms;

import io.github.luigeneric.utils.BgoRandom;

public interface DamageCalcStrategy
{
    float getDamageRoll(final BgoRandom bgoRandom, final float min, final float max);
}
