package io.github.luigeneric.core.protocols.universe;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage implements IProtocolWrite
{
    Update(7);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeMsgType(this.intValue);
    }

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
