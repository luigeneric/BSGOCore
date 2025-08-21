package io.github.luigeneric.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FactorType
{
    Experience(1),
        PVP_XP(7),
        PVE_XP(8),
        Reward_ASSIGNMENT_XP(9),
        DutyXP(10), //ftl mission

    Loot(3), //on everything from loot except merits and asteroid-loot-type
    AsteroidYield(4),
    MeritIncome(5),
    MeritCapacity(6),

    MissionReward(11), //dradis
    WaterCapacity(12),

    @Deprecated
    SkillLearning(2);

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, FactorType> mappings = new HashMap<>();
    }

    private static Map<Integer, FactorType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    FactorType(final int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static FactorType forValue(int value)
    {
        return getMappings().get(value);
    }

    public static List<FactorType> getChildrenForExp()
    {
        final List<FactorType> factorTypes = new ArrayList<>();
        factorTypes.add(PVE_XP);
        factorTypes.add(PVP_XP);
        factorTypes.add(Reward_ASSIGNMENT_XP);
        factorTypes.add(DutyXP);

        return factorTypes;
    }
}
