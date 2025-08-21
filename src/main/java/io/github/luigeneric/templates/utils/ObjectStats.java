package io.github.luigeneric.templates.utils;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ObjectStats implements IProtocolWrite
{
    protected final Map<ObjectStat, Float> stats;

    public ObjectStats(Map<ObjectStat, Float> stats)
    {
        this.stats = stats;
    }
    public ObjectStats(final ObjectStats stats)
    {
        this(stats.getAllStats());
    }
    public ObjectStats()
    {
        this(new HashMap<>());
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        final int len = this.stats.size();
        bw.writeLength(len);
        for (final Map.Entry<ObjectStat, Float> stat : stats.entrySet())
        {
            //bw.writeByte((byte) 1); //now in spacesubscribeinfo
            bw.writeUInt16(stat.getKey().value);
            bw.writeSingle(stat.getValue());
        }
    }

    @Override
    public String toString()
    {
        return "ObjectStats{" +
                "stats=" + stats +
                '}';
    }

    public Map<ObjectStat, Float> getAllStats()
    {
        return this.stats;
    }
    public ObjectStats getCopy()
    {
        return new ObjectStats(new HashMap<>(this.stats));
    }

    public Float getStat(final ObjectStat objectStat)
    {
        return this.stats.get(objectStat);
    }

    public float getStatOrDefault(final ObjectStat objectStat, final float orValue)
    {
        return this.stats.getOrDefault(objectStat, orValue);
    }
    public float getStatOrDefault(final ObjectStat objectStat)
    {
        return this.stats.getOrDefault(objectStat, 0f);
    }
    public void setStat(final ObjectStat objectStat, final float newValue)
    {
        this.stats.put(objectStat, newValue);
    }
    public void setStats(final ObjectStats stats)
    {
        this.stats.putAll(stats.getAllStats());
    }

    public boolean containsStat(final ObjectStat key)
    {
        return this.stats.containsKey(key);
    }
    public void clean()
    {
        this.stats.clear();
    }

    public void removeStat(final ObjectStat stat)
    {
        this.stats.remove(stat);
    }

    public void put(final ObjectStats copy)
    {
        //this.stats.clear();
        this.stats.putAll(copy.getAllStats());
    }


    public static ObjectStats mapObjectStats(final ObjectStats objectStats)
    {
        final ObjectStats rv = new ObjectStats();
        for (Map.Entry<ObjectStat, Float> stat : objectStats.getAllStats().entrySet())
        {
            if (stat.getKey() == ObjectStat.TurnSpeed)
            {
                rv.setStat(ObjectStat.PitchMaxSpeed, stat.getValue());
                rv.setStat(ObjectStat.YawMaxSpeed, stat.getValue());

                continue;
            }
            if (stat.getKey() == ObjectStat.TurnAcceleration)
            {
                rv.setStat(ObjectStat.PitchAcceleration, stat.getValue());
                rv.setStat(ObjectStat.YawAcceleration, stat.getValue());

                continue;
            }

            rv.setStat(stat.getKey(), stat.getValue());
        }
        return rv;
    }

    public static ObjectStats getStatsMultiplyBonus(final ObjectStats stats, final ObjectStats multipliers)
    {
        final ObjectStats returnStats = new ObjectStats();
        final ObjectStats mappedMultipliers = mapObjectStats(multipliers);

        for (Map.Entry<ObjectStat, Float> multStat : mappedMultipliers.getAllStats().entrySet())
        {
            if (stats.containsStat(multStat.getKey()))
            {
                final float newValue = stats.getStat(multStat.getKey()) * multStat.getValue();
                final float deltaValue = newValue - stats.getStat(multStat.getKey());
                returnStats.setStat(multStat.getKey(), deltaValue);
            }
        }
        return returnStats;
    }

    /**
     * Applies the first stat as additive to the second arg
     * @param statsToAdd ObjectStat to additive usage
     * @param toApplyOn Stats with the result in
     */
    public static void applyStatsAddTo(final ObjectStats statsToAdd, final ObjectStats toApplyOn)
    {
        final Map<ObjectStat, Float> toApplyOnStats = toApplyOn.getAllStats();
        if (toApplyOnStats == null) return;

        for (Map.Entry<ObjectStat, Float> statToAdd : statsToAdd.getAllStats().entrySet())
        {
            if (toApplyOnStats.containsKey(ObjectStat.TurnSpeed))
            {
                log.error("Inside ObjectStats: contains turnSpeed but not map");
            }
            if (toApplyOnStats.containsKey(statToAdd.getKey()))
            {
                final float newValue = toApplyOnStats.get(statToAdd.getKey()) + statToAdd.getValue();
                toApplyOnStats.put(statToAdd.getKey(), newValue);
            }
        }
    }

    public static void applyStatsMultTo(final ObjectStats objectStats, final ObjectStats toApplyOn)
    {
        final Map<ObjectStat, Float> toApplyOnStats = toApplyOn.getAllStats();
        if (toApplyOnStats == null) return;

        for (Map.Entry<ObjectStat, Float> stat : objectStats.getAllStats().entrySet())
        {
            if (toApplyOnStats.containsKey(stat.getKey()))
            {
                float newValue = toApplyOnStats.get(stat.getKey()) * stat.getValue();
                toApplyOnStats.put(stat.getKey(), newValue);
            }
        }
    }
    public static ObjectStats applyStatsMultToIfBonusExistsInApplyOn(final ObjectStats bonusStats, final ObjectStats toApplyOn)
    {
        final ObjectStats rv = new ObjectStats();
        final Map<ObjectStat, Float> toApplyOnStats = toApplyOn.getAllStats();
        if (toApplyOnStats == null) return rv;

        for (Map.Entry<ObjectStat, Float> bonusStat : bonusStats.getAllStats().entrySet())
        {
            if (toApplyOnStats.containsKey(bonusStat.getKey()))
            {
                rv.setStat(bonusStat.getKey(), toApplyOnStats.get(bonusStat.getKey()) * bonusStat.getValue());
            }
        }
        return rv;
    }
}
