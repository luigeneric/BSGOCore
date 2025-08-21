package io.github.luigeneric.enums;

public enum StaticCardGUID
{
    StickerList(166885587L),
    Avatar(109873795L),
    GalaxyMap(150576033L),
    GlobalCard(49842157L),

    ColonialStarterShip(2366349390L),
    CylonStarterShip(1427261742),

    CiCColonial(3608851L),
    CiCCylon(259498852L),
    RoomOutpostColonial(151517344L),
    RoomOutpostCylon(151517343L),
    NeutralRewardCard(1L),

    ShipListCardColonial(73551268L),
    ShipListCardCylon(188756164L),

    MissileMiniNuke(244685066L),
    MissileNuke(253392099L),
    MissileCard(117216909L),

    cylonstationary1(1783473196L),
    cylonstationary2(1783473197L),
    cylonstationary3(1783473198L),
    cylonstationary4(1783473199L),
    cylonstationary5(1783473200L),
    cylonstationary6(1783473201L),

    humanstationary1(1783473190L),
    humanstationary2(1783473191L),
    humanstationary3(1783473192L),
    humanstationary4(1783473193L),
    humanstationary5(1783473194L),
    humanstationary6(1783473195L),

    OutpostCylon(1450701611L),
    OutpostColonial(1450701610L);

    public static final int SIZE = Integer.SIZE;

    private long intValue;
    private static java.util.HashMap<Long, StaticCardGUID> mappings;
    private static java.util.HashMap<Long, StaticCardGUID> getMappings()
    {
        if (mappings == null)
        {
            synchronized (StaticCardGUID.class)
            {
                if (mappings == null)
                {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    StaticCardGUID(long value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public long getValue()
    {
        return intValue;
    }

    public static StaticCardGUID forValue(long value)
    {
        return getMappings().get(value);
    }
}
