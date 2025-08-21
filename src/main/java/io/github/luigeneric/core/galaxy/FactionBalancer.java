package io.github.luigeneric.core.galaxy;


import io.github.luigeneric.enums.Faction;

import java.util.concurrent.TimeUnit;

public interface FactionBalancer
{
    void setCountFaction(final Faction faction, final long count);
    float getFactionBonus(final Faction forFaction);

    long time();
    TimeUnit timeUnit();
}
