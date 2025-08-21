package io.github.luigeneric.templates.loot;


import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.List;

public class LootDamageTemplate extends LootTemplate
{
    /**
     * @param id             the identifier
     * @param rewardCount    how many entries will be rolled
     * @param experience
     * @param chance
     * @param lootEntryInfos RewardCardGUID
     *                       Chance in full "points"
     *                       level intervall
     */
    public LootDamageTemplate(long id, int rewardCount, long experience, float chance, List<LootEntryInfo> lootEntryInfos)
    {
        super(id, LootTemplateType.Damage, rewardCount, new short[]{0, 255}, experience, chance, lootEntryInfos);
    }

    public static LootTemplate ofSingle(final ItemCountable ressource)
    {
        return new LootDamageTemplate(-1, 1, 50, 1,
                List.of(new LootEntryInfo(1, new short[]{0, 255}, ressource, 0)));
    }
}
