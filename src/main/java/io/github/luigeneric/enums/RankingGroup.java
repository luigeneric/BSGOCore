package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum RankingGroup
{
    Kills((short)1),
    VictoriesDefeatRatio(((short)1) + 1),
    VictoriesHour(((short)1) + 2),
    Mining(((short)1) + 3),
    Progression(((short)1) + 4),
    Objectives(((short)1) + 5),
    PlanetoidMining(((short)1) + 6),
    Wave(((short)1) + 7),
    Arena1vs1(((short)1) + 8),
    Arena3vs3(((short)1) + 9),
    KillActions(((short)1) + 10),
    SupportActions(((short)1) + 11),
    DamageActions(((short)1) + 12);

    public static final int SIZE = Short.SIZE;

    public final short value;

    private static final class MappingsHolder
    {
        private static final Map<Short, RankingGroup> mappings = new HashMap<>();
    }

    private static Map<Short, RankingGroup> getMappings()
    {
        return MappingsHolder.mappings;
    }

    RankingGroup(final short value)
    {
        this.value = value;
        getMappings().put(value, this);
    }
    RankingGroup(final int value)
    {
        this((short) value);
    }

    public static RankingGroup forValue(final short value)
    {
        return getMappings().get(value);
    }
}
