package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum SpeedMode
{
    None(-1),
    Abs(0),
    Delta(1),
    Stop(2),
    Full(3);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, SpeedMode> mappings = new HashMap<>();
    }

    private static Map<Integer, SpeedMode> getMappings()
    {
        return MappingsHolder.mappings;
    }

    SpeedMode(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static SpeedMode forValue(int value)
    {
        return getMappings().get(value);
    }
}
