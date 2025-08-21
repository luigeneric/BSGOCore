package io.github.luigeneric.core.protocols.debug;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Command(2),
    Message(3),
    Counters(9),
    ProcessState(15),
    UpdateRoles(16);

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Integer, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(final int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static ServerMessage forValue(int value)
    {
        return getMappings().get(value);
    }
}
