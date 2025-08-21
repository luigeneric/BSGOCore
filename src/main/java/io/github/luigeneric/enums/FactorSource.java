package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum FactorSource
{
    Augment(1),
    /**
     * Faction bonus for starting
     */
    Faction(2),
    /**
     * Syfy accounts
     */
    Marketing(3),
    @Deprecated
    FactionSwitch(4),
    //@Deprecated
    Holiday(5);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, FactorSource> mappings = new HashMap<>();
    }

    private static Map<Integer, FactorSource> getMappings()
    {
        return MappingsHolder.mappings;
    }

    FactorSource(final int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static FactorSource forValue(int value)
    {
        return getMappings().get(value);
    }
}
