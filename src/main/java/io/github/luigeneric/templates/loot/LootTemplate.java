package io.github.luigeneric.templates.loot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public abstract class LootTemplate
{
    protected final long id;
    protected final LootTemplateType type;
    /**
     * how many entries will be rolled
     */
    protected final int rewardCount;
    protected final short[] globalLevelIntervall;
    protected final long experience;
    protected final float chance;
    protected final List<LootEntryInfo> lootEntryInfos;

    protected LootTemplate(long id, LootTemplateType type, int rewardCount, short[] globalLevelIntervall, long experience, float chance, List<LootEntryInfo> lootEntryInfos)
    {
        this.id = id;
        this.type = type;
        this.rewardCount = rewardCount;
        this.globalLevelIntervall = globalLevelIntervall;
        this.experience = experience;
        this.chance = chance;
        this.lootEntryInfos = lootEntryInfos;
    }

    public short[] getGlobalLevelIntervall()
    {
        return Objects.requireNonNullElseGet(this.globalLevelIntervall, () -> new short[]{0, 255});
    }
    public boolean isInGlobalLevel(final short level)
    {
        if (this.globalLevelIntervall == null)
        {
            return true;
        }
        if (globalLevelIntervall.length != 2)
        {
            return true;
        }
        return level >= globalLevelIntervall[0] && level <= globalLevelIntervall[1];
    }

    public long getId()
    {
        return id;
    }

    public LootTemplateType getType()
    {
        return type;
    }

    public int getRewardCount()
    {
        return rewardCount;
    }

    public long getExperience()
    {
        return experience;
    }

    public float getChance()
    {
        return chance;
    }

    public List<LootEntryInfo> getLootEntryInfos()
    {
        return lootEntryInfos;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LootTemplate that = (LootTemplate) o;

        if (id != that.id) return false;
        if (rewardCount != that.rewardCount) return false;
        if (experience != that.experience) return false;
        if (Float.compare(that.chance, chance) != 0) return false;
        if (type != that.type) return false;
        if (!Arrays.equals(globalLevelIntervall, that.globalLevelIntervall)) return false;
        return Objects.equals(lootEntryInfos, that.lootEntryInfos);
    }

    @Override
    public int hashCode()
    {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + rewardCount;
        result = 31 * result + Arrays.hashCode(globalLevelIntervall);
        result = 31 * result + (int) (experience ^ (experience >>> 32));
        result = 31 * result + (chance != +0.0f ? Float.floatToIntBits(chance) : 0);
        result = 31 * result + (lootEntryInfos != null ? lootEntryInfos.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "LootTemplate{" +
                "id=" + id +
                ", type=" + type +
                ", rewardCount=" + rewardCount +
                ", globalLevelIntervall=" + Arrays.toString(globalLevelIntervall) +
                ", experience=" + experience +
                ", chance=" + chance +
                ", lootEntryInfos=" + lootEntryInfos +
                '}';
    }
}
