package io.github.luigeneric.templates.loot;

import java.util.List;

public class LootRadiusTemplate extends LootTemplate
{
    protected final float radius;
    public LootRadiusTemplate(long id, int rewardCount, int experience, float chance,
                              List<LootEntryInfo> lootEntryInfos, float radius)
    {
        super(id, LootTemplateType.RadiusDamage, rewardCount, new short[]{0, 255}, experience, chance, lootEntryInfos);
        this.radius = radius;
    }

    public float getRadius()
    {
        return radius;
    }
}
