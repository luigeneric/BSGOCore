package io.github.luigeneric.core.spaceentities.statsinfo.buffer;

public enum SpaceUpdateType
{
    ObjectStat(1),
    AddBuff(2),
    CombatStatus(3),
    TargetID(4),
    RemoveBuff(5),
    PowerPoints(6),
    HullPoints(7),
    Reset(9),
    SlotStat(12),
    ShipAspect(13),
    AddToggleBuff(14),
    RemoveToggleBuff(15),
    AddStatsModifier(16),
    RemoveBuffGUID(17),
    ShortCircuited(18),
    ShortRemovedGUID(19),
    CaptureStatus(20),
    AddSectorModifier(21),
    RemoveSectorModifier(22),
    VitalPointsChanged(23);




    public static final Byte SIZE = Byte.SIZE;

    private byte value;
    private static java.util.HashMap<Byte, SpaceUpdateType> mappings;

    SpaceUpdateType(int i)
    {
        this((byte) i);
    }

    private static java.util.HashMap<Byte, SpaceUpdateType> getMappings()
    {
        if (mappings == null)
        {
            synchronized (SpaceUpdateType.class)
            {
                if (mappings == null)
                {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    SpaceUpdateType(byte value)
    {
        this.value = value;
        getMappings().put(value, this);
    }

    public byte getValue()
    {
        return this.value;
    }

    public static SpaceUpdateType forValue(final byte value)
    {
        return getMappings().get(value);
    }
}
