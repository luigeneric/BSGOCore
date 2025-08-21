package io.github.luigeneric.core.sector.management.relation;

import java.util.HashMap;
import java.util.Map;

public enum Relation
{
    Friend(1),
    Enemy(2),
    Neutral(3),
    Self(4);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, Relation> mappings = new HashMap<>();
    }

    private static Map<Integer, Relation> getMappings()
    {
        return MappingsHolder.mappings;
    }

    Relation(final int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public static Relation forValue(int value)
    {
        return getMappings().get(value);
    }
}
