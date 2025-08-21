package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum GuildOperationResult
{
    Ok((byte)1),
    InvalidPermissions(((byte)1) + 1),
    NotInGuild(((byte)1) + 2),
    ErrorLeaderPermissions(((byte)1) + 3);

    public static final int SIZE = Byte.SIZE;

    private final byte byteValue;
    private static Map<Byte, GuildOperationResult> mappings;

    GuildOperationResult(int i)
    {
        this((byte) i);
    }

    private static Map<Byte, GuildOperationResult> getMappings()
    {
        if (mappings == null)
        {
            synchronized (GuildOperationResult.class)
            {
                if (mappings == null)
                {
                    mappings = new HashMap<>();
                }
            }
        }
        return mappings;
    }

    GuildOperationResult(final byte value)
    {
        byteValue = value;
        getMappings().put(value, this);
    }

    public byte getValue()
    {
        return byteValue;
    }

    public static GuildOperationResult forValue(byte value)
    {
        return getMappings().get(value);
    }
}
