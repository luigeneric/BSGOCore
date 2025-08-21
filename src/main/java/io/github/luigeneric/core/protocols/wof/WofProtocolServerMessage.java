package io.github.luigeneric.core.protocols.wof;

import java.util.HashMap;
import java.util.Map;

public enum WofProtocolServerMessage
{
    ReplyInit(2),
    ReplyDraw(4),
    ReplyVisibleMaps(6),
    ReplyMapInfo(9);

    public static final int SIZE = Short.SIZE;

    public final short shortValue;

    private static final class MappingsHolder
    {
        private static final Map<Short, WofProtocolServerMessage> mappings = new HashMap<>();
    }

    private static Map<Short, WofProtocolServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    WofProtocolServerMessage(final int value)
    {
        this((short) value);
    }

    WofProtocolServerMessage(final short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    public short getValue()
    {
        return shortValue;
    }

    public static WofProtocolServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
