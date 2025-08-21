package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum RemovingCause
{
    Disconnection(1),
    Death(2),
    JumpOut(3),
    TTL(4),
    Dock(5),
    Hit(6),
    JustRemoved(7),
    Collected(8);

    public static final int SIZE = Byte.SIZE;

    public final byte byteValue;

    RemovingCause(final int i)
    {
        this((byte)i);
    }
    RemovingCause(final byte value)
    {
        byteValue = value;
        getMappings().put(value, this);
    }

    public boolean isOfType(final RemovingCause... removingCause)
    {
        for (final RemovingCause cause : removingCause)
        {
            if (this == cause)
            {
                return true;
            }
        }
        return false;
    }

    private static final class MappingsHolder
    {
        private static final Map<Byte, RemovingCause> mappings = new HashMap<>();
    }

    private static Map<Byte, RemovingCause> getMappings()
    {
        return MappingsHolder.mappings;
    }


    public static RemovingCause forValue(final byte value)
    {
        return getMappings().get(value);
    }
}
