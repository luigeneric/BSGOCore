package io.github.luigeneric.templates.loot;

import java.util.List;

public class LootDamageRadiusTemplate extends LootRadiusTemplate
{
    private final float minDamage;
    public LootDamageRadiusTemplate(long id, int rewardCount, int experience, float chance, List<LootEntryInfo> lootEntryInfos, float radius, float minDamage)
    {
        super(id, rewardCount, experience, chance, lootEntryInfos, radius);
        this.minDamage = minDamage;
    }

    public float getMinDamage()
    {
        return minDamage;
    }

    /**
     * If mindamage is 0, you'll receive always loot
     * @return
     */
    public boolean hasMinimumDamage()
    {
        return minDamage != 0;
    }

    /**
     * Has a limit if radius is not 0, if radius is equal to 0, there is no radius limit
     * @return
     */
    public boolean hasRadiusLimit()
    {
        return radius != 0;
    }
}
