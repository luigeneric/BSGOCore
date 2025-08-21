package io.github.luigeneric.templates.utils;

import java.util.Collections;
import java.util.Map;

public class UnmodifiableObjectStats extends ObjectStats
{
    public UnmodifiableObjectStats(final Map<ObjectStat, Float> stats)
    {
        super(Collections.unmodifiableMap(stats));
    }

    public UnmodifiableObjectStats(ObjectStats stats)
    {
        this(stats.stats);
    }
}
