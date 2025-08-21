package io.github.luigeneric.utils;

import io.github.luigeneric.enums.Faction;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TimestampedCounter
{
    private final Faction faction;
    private final Map<Instant, Integer> timestamps;

    public TimestampedCounter(final Faction faction)
    {
        this.faction = faction;
        timestamps = new ConcurrentHashMap<>();
    }

    public void addTimestamp()
    {
        timestamps.compute(Instant.now(), (key, value) -> (value == null) ? 1 : value + 1);
    }
    public void addTimeStamp(final int num)
    {
        timestamps.compute(Instant.now(), (key, value) -> (value == null) ? num : value + num);
    }

    public void removeOldTimestamps()
    {
        Instant fiveMinutesAgo = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(5));
        for (Iterator<Map.Entry<Instant, Integer>> it = timestamps.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<Instant, Integer> entry = it.next();
            if (entry.getKey().isBefore(fiveMinutesAgo))
            {
                it.remove();
            }
        }
    }

    public int getCount()
    {
        removeOldTimestamps();
        return timestamps.size();
    }

    public Faction getFaction()
    {
        return faction;
    }
}