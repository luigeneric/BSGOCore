package io.github.luigeneric.core.spaceentities.bindings;

public enum ModuleBindingGUID
{
    turret_tier1(210609778L),
    turret_tier2(210609779L),
    turret_tier3(210609780L),
    turret_tier4(210609781L),
    missile_tier1(249318994L),
    missile_tier2(249318995L),
    missile_tier3(249318996L),
    missile_tier4(249318997L);

    public static final int SIZE = Long.SIZE;

    private long longValue;
    private static java.util.HashMap<Long, ModuleBindingGUID> mappings;
    private static java.util.HashMap<Long, ModuleBindingGUID> getMappings()
    {
        if (mappings == null)
        {
            synchronized (ModuleBindingGUID.class)
            {
                if (mappings == null)
                {
                    mappings = new java.util.HashMap<Long, ModuleBindingGUID>();
                }
            }
        }
        return mappings;
    }

    ModuleBindingGUID(long value)
    {
        longValue = value;
        getMappings().put(value, this);
    }

    public long getValue()
    {
        return longValue;
    }

    public static ModuleBindingGUID forValue(long value)
    {
        return getMappings().get(value);
    }
}
