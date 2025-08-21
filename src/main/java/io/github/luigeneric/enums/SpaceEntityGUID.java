package io.github.luigeneric.enums;

public enum SpaceEntityGUID
{

    ColonialMiningShip(432435645L),
    CylonMiningShip(432435644L);

    public static final int SIZE = Integer.SIZE;

    private long intValue;
    private static java.util.HashMap<Long, SpaceEntityGUID> mappings;
    private static java.util.HashMap<Long, SpaceEntityGUID> getMappings()
    {
        if (mappings == null)
        {
            synchronized (SpaceEntityGUID.class)
            {
                if (mappings == null)
                {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    SpaceEntityGUID(long value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public long getValue()
    {
        return intValue;
    }

    public static SpaceEntityGUID forValue(long value)
    {
        return getMappings().get(value);
    }
}
