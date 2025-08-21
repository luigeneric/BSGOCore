package io.github.luigeneric.core.player.counters;


import io.github.luigeneric.templates.cards.CounterCardType;

public record CounterFacade(Counters counters, MissionBook missionBook)
{
    public void incrementCounter(final CounterCardType counterCardType, final long sectorCardGuid)
    {
        incrementCounter(counterCardType, sectorCardGuid, 1);
    }

    public void incrementCounter(final CounterCardType counterCardType, final long sectorCardGuid, final double byValue)
    {
        incrementCounter(counterCardType.cardGuid, sectorCardGuid, byValue);
    }

    public void incrementCounter(final long counterCardGuid, final long sectorCardGuid, final double byValue)
    {
        this.counters.addCounterOf(counterCardGuid, byValue);
        this.missionBook.incrementCountByForAll(counterCardGuid, sectorCardGuid, byValue);
    }
    public void setCounter(final long counterCardGuid, final long value)
    {
        this.counters.injectOldCounters(counterCardGuid, value);
    }

    public void initAllUpdate()
    {
        this.counters.initAllUpdate();
        this.missionBook.initAllUpdate();
    }
}
