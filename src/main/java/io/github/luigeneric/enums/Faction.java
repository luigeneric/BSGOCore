package io.github.luigeneric.enums;

import java.util.HashMap;
import java.util.Map;

public enum Faction
{
    Neutral     (0, 0x00000000L),
    Colonial    (1, 0x40000000L),
    Cylon       (2, 0x80000000L),
    Ancient     (3, 0xc0000000L);


    public final byte value;
    public final long mask;
    private static final Map<Byte, Faction> map = new HashMap<>();
    private static final Map<Long, Faction> mapMask = new HashMap<>();
    Faction(final int value, final long mask)
    {
        this.value = (byte) value;
        this.mask = mask;
    }
    static {
        for (Faction faction : Faction.values()) {
            map.put(faction.value, faction);
            mapMask.put(faction.mask, faction);
        }
    }
    public static Faction valueOf(final int num)
    {
        return valueOf((byte) num);
    }
    public static Faction valueOf(final byte pageType)
    {
        return map.get(pageType);
    }
    public static Faction forMask(final long mask)
    {
        return mapMask.get(mask);
    }

    public static Faction invert(final Faction faction)
    {
        switch (faction)
        {
            case Colonial ->
            {
                return Cylon;
            }
            case Cylon ->
            {
                return Colonial;
            }
        }
        return faction;
    }

    public boolean isEnemyFaction(final Faction other)
    {
        return this != other;
    }

    public Faction extractFaction(long objectID)
    {
        objectID = objectID & 0xC0000000L;
        if (3221225472L == objectID)
        {
            return Faction.Ancient;
        }
        if (1073741824L == objectID)
        {
            return Faction.Colonial;
        }
        if (2147483648L == objectID)
        {
            return Faction.Cylon;
        }
        if (0 == objectID)
        {
            return Faction.Neutral;
        }
        throw new IllegalStateException("should never happen");
    }

    public Faction enemyFaction() throws IllegalStateException
    {
        return switch (this)
        {
            case Colonial -> Cylon;
            case Cylon -> Colonial;
            default -> throw new IllegalStateException("This should never happen");
        };
    }
}
