package io.github.luigeneric.core.player.counters;

import java.util.HashMap;
import java.util.Map;

public enum CounterGUIDs
{
    arena(6851650),
    asteroids_scanned(10577685L),
    debris_looted(11402165),
    pvp_action_savior(21363731),
    total_deaths(35463892),
    pvp_action_assist(39302357);
    //aesir killer guid, objectkey


    public final long value;
    private static final Map<Long, CounterGUIDs> map = new HashMap<>();

    CounterGUIDs(final long value)
    {
        this.value = value;
    }

    static {
        for (CounterGUIDs pageType : CounterGUIDs.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static CounterGUIDs valueOf(final long pageType)
    {
        return map.get(pageType);
    }
}
