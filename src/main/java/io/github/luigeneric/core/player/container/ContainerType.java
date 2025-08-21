package io.github.luigeneric.core.player.container;

import java.util.HashMap;
import java.util.Map;

public enum ContainerType
{
    Hold(1),
    Locker(2),
    ShipSlot(3),
    Shop(4),
    Loot(5),
    BlackHole(6),
    Mail(7),
    EventShop(8);

    public static final int SIZE = Integer.SIZE;

    public final int value;

    private static final class MappingsHolder
    {
        private static final Map<Integer, ContainerType> mappings = new HashMap<>();
    }

    private static Map<Integer, ContainerType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ContainerType(int value)
    {
        this.value = value;
        getMappings().put(value, this);
    }

    public static ContainerType forValue(int value)
    {
        return getMappings().get(value);
    }
}
