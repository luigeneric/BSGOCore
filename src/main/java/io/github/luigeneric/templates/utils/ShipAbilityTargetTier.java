package io.github.luigeneric.templates.utils;

import java.util.HashMap;
import java.util.Map;

public enum ShipAbilityTargetTier
{
    Tier1(1),
    Tier2(2),
    Tier3(4),
    Tier4(8),
    Any(16);

    public static final int SIZE = Integer.SIZE;

    public final int value;

    private static final class MappingsHolder
    {
        private static final Map<Integer, ShipAbilityTargetTier> mappings = new HashMap<>();
    }

    private static Map<Integer, ShipAbilityTargetTier> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ShipAbilityTargetTier(int value)
    {
        this.value = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return value;
    }

    public static ShipAbilityTargetTier forValue(int value)
    {
        return getMappings().get(value);
    }
}
