package io.github.luigeneric.core.protocols.subscribe;

import java.util.HashMap;
import java.util.Map;

public enum InfoType
{
    Name(1),
    Faction(2),
    Avatar(4),
    Wing(8),
    Ships(16),
    /**
     * @Deprecated use Location instead
     */
    @Deprecated
    Status(32),
    Level(64),
    Title(128),
    Location(256),
    Medal(512),
    Stats(1024),
    Logout(2048),
    /**
     * @Deprecated implemented in ZoneProtocol
     */
    @Deprecated
    TournamentIndicator(4096);



    public static final int SIZE = Integer.SIZE;

    private final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, InfoType> mappings = new HashMap<>();
    }

    private static Map<Integer, InfoType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    InfoType(final int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static InfoType forValue(final int value)
    {
        return getMappings().get(value);
    }
}
