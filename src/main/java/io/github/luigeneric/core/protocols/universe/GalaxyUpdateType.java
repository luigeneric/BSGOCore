package io.github.luigeneric.core.protocols.universe;

import java.util.HashMap;
import java.util.Map;

public enum GalaxyUpdateType
{
    Rcp(1),
    ConquestLocation(2),
    ConquestPrice(3),
    SectorOutpostPoints(4),
    SectorMiningShips(5),
    SectorPvPKills(6),
    SectorDynamicMissions(7),
    SectorOutpostState(8),
    SectorBeaconState(9),
    SectorJumpTargetTransponders(10),
    SectorPlayerSlots(11);

    public static final int SIZE = Byte.SIZE;

    public final byte byteValue;

    GalaxyUpdateType(int i)
    {
        this((byte) i);
    }

    private static final class MappingsHolder
    {
        private static final Map<Byte, GalaxyUpdateType> mappings = new HashMap<>();
    }

    private static Map<Byte, GalaxyUpdateType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    GalaxyUpdateType(final byte value)
    {
        byteValue = value;
        getMappings().put(value, this);
    }

    public byte getValue()
    {
        return byteValue;
    }

    public static GalaxyUpdateType forValue(byte value)
    {
        return getMappings().get(value);
    }
}
