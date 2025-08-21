package io.github.luigeneric.enums;


import java.util.HashMap;
import java.util.Map;

public enum GuildOperation
{
    ChangePermissions(1),
    ChangeRankNames(2),
    Invite(3),
    PromoteDemote(4),
    KickMember(5),
    OfficerChat(6);

    public static final int SIZE = Short.SIZE;

    public final short shortValue;

    GuildOperation(int i)
    {
        this((short) i);
    }

    private static final class MappingsHolder
    {
        private static final Map<Short, GuildOperation> mappings = new HashMap<>();
    }

    private static Map<Short, GuildOperation> getMappings()
    {
        return MappingsHolder.mappings;
    }

    GuildOperation(final short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    public short getValue()
    {
        return shortValue;
    }
    public long getBitmask()
    {
        return 1L << (this.shortValue - 1);
    }

    public static GuildOperation forValue(short value)
    {
        return getMappings().get(value);
    }
}
