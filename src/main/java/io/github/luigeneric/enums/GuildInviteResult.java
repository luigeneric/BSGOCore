package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum GuildInviteResult
{
    AlreadyInGuild(2),
    Timeout(3),
    Refuse(4),
    Accept(5),
    FullGuild(7);

    public static final int SIZE = Byte.SIZE;

    public final byte byteValue;

    GuildInviteResult(int i)
    {
        this((byte) i);
    }

    private static final class MappingsHolder
    {
        private static final Map<Byte, GuildInviteResult> mappings = new HashMap<>();
    }

    private static Map<Byte, GuildInviteResult> getMappings()
    {
        return MappingsHolder.mappings;
    }

    GuildInviteResult(final byte value)
    {
        byteValue = value;
        getMappings().put(value, this);
    }

    public byte getValue()
    {
        return byteValue;
    }

    public static GuildInviteResult forValue(byte value)
    {
        return getMappings().get(value);
    }
}
