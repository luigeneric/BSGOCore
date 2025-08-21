package io.github.luigeneric.core.database;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerID with Guid and counter
 * @param playerId
 * @param counters
 */
public record CounterRecord(long playerId, Map<Long, Double> counters)
{
    public CounterRecord(long playerId)
    {
        this(playerId, new HashMap<>());
    }
}
