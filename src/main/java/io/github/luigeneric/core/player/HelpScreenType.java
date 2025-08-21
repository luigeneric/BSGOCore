package io.github.luigeneric.core.player;

import java.util.HashMap;
import java.util.Map;

public enum HelpScreenType
{
    First(0),
    BasicControls(0),
    AdvancedControls(1),
    NPCInteraction(2),
    DailyAssignments(3),
    FTLJump(4),
    Mining(5),
    StoryMissions(6),
    IndustrialMining(7),
    Duties(8),
    BuyingNewShip(9),
    Attacking(10),
    Shop(11),
    Repair(12),
    UpgradeSystems(13),
    BuyingCubits(14),
    DradisContact(15),
    FTLMission(16),
    Last(16);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, HelpScreenType> mappings = new HashMap<>();
    }

    private static Map<Integer, HelpScreenType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    HelpScreenType(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public static HelpScreenType forValue(int value)
    {
        return getMappings().get(value);
    }
}
