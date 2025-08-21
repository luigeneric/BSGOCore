package io.github.luigeneric.core.protocols.universe;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    SubscribeGalaxyMap(5),
    UnsubscribeGalaxyMap(6);

    public static final int SIZE = Short.SIZE;

    public final int value;


    private static final class MappingsHolder
    {
        private static final Map<Integer, ClientMessage> mappings = new HashMap<>();
    }

    private static Map<Integer, ClientMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ClientMessage(final int value)
    {
        this.value = value;
        getMappings().put(value, this);
    }

    public static ClientMessage forValue(int value)
    {
        return getMappings().get(value);
    }
}
