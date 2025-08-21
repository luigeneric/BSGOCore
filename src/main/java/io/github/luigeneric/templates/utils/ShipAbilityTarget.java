package io.github.luigeneric.templates.utils;

import java.util.HashMap;
import java.util.Map;

public enum ShipAbilityTarget
{
    Asteroid(1),
    Ship(2),
    Any(4),
    Missile(8),
    Planetoid(16),
    Mine(32),
    JumpTargetTransponder(64),
    Comet(128);

    public final byte value;


    private static Map<Byte, ShipAbilityTarget> mappings;


    private static Map<Byte, ShipAbilityTarget> getMapping()
    {
        if (mappings == null)
        {
            synchronized (ShipAbilityTarget.class)
            {
                if (mappings == null)
                {
                    mappings = new HashMap<>();
                }
            }
        }
        return mappings;
    }


    public static ShipAbilityTarget forValue(final byte num)
    {
        return mappings.get(num);
    }


    public byte getValue()
    {
        return value;
    }

    ShipAbilityTarget(int value)
    {
        this.value = (byte) value;
        getMapping().put((byte) value, this);
    }
}
