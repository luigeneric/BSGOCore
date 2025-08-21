package io.github.luigeneric.templates.loot;

import java.util.HashMap;
import java.util.Map;

/**
 * The first 99 ids are for static loot ids
 */
public enum StaticLootId
{
    Missile(1),
    MiningShip(2),
    Comet(3),

    PvpKillGrey(20),
    PvpKillYellow(21),
    PvpKillWhite(22),
    PvpKillRed(23);

    public static final int SIZE = Short.SIZE;

    public final long value;


    private static final class MappingsHolder
    {
        private static final Map<Long, StaticLootId> mappings = new HashMap<>();
    }

    private static Map<Long, StaticLootId> getMappings()
    {
        return MappingsHolder.mappings;
    }

    StaticLootId(final long value)
    {
        this.value = value;
        getMappings().put(this.value, this);
    }

    public static StaticLootId forValue(long value)
    {
        return getMappings().get(value);
    }

    public static StaticLootId getFromLevel(final short from, final short to)
    {
        final float modifier = getModifier(from, to);
        if (modifier < 0.25f)
        {
            return PvpKillGrey;
        }
        else if (modifier < 0.8f)
        {
            return PvpKillYellow;
        }
        else if (modifier > 1.2f)
        {
            return PvpKillRed;
        }
        return PvpKillWhite;
    }
    private static float getModifier(final short from, final short to)
    {
        if (from == 0)
            return 1.5f;
        return (float) to / (float) from;
    }
}
