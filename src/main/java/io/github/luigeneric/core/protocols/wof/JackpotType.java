package io.github.luigeneric.core.protocols.wof;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.util.HashMap;
import java.util.Map;

public enum JackpotType implements IProtocolWrite
{
    Item((short) 1),
    MapPart(((short) 2));

    public static final int SIZE = Short.SIZE;

    public final short shortValue;

    private static final class MappingsHolder
    {
        private static final Map<Short, JackpotType> mappings = new HashMap<>();
    }

    private static Map<Short, JackpotType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    JackpotType(short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    public short getValue()
    {
        return shortValue;
    }

    public static JackpotType forValue(short value)
    {
        return getMappings().get(value);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.shortValue);
    }
}
