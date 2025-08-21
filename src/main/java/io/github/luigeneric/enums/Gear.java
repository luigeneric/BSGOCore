package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum Gear
{
    None(-1),
    Regular(0),
    Boost(1),
    RCS(2);

    public static final int SIZE = Integer.SIZE;

    public final byte byteValue;

    private static final class MappingsHolder
    {
        private static final Map<Byte, Gear> mappings = new HashMap<>();
    }

    private static Map<Byte, Gear> getMappings()
    {
        return MappingsHolder.mappings;
    }

    Gear(final int value)
    {
        this.byteValue = (byte) value;
        getMappings().put((byte) value, this);
    }

    public byte getValue()
    {
        return byteValue;
    }

    public static Gear forValue(final byte value)
    {
        return getMappings().get(value);
    }
}

