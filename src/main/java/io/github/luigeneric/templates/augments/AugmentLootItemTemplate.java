package io.github.luigeneric.templates.augments;


import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.utils.AugmentActionType;

import java.util.List;

public class AugmentLootItemTemplate extends AugmentTemplate
{
    private final long experience;
    private final List<LootEntryInfo> lootEntryInfos;
    public AugmentLootItemTemplate(final long associatedItemGUID, long experience, List<LootEntryInfo> lootEntryInfos)
    {
        super(AugmentActionType.LootItem, associatedItemGUID);
        this.experience = experience;
        this.lootEntryInfos = lootEntryInfos;
    }

    public long getExperience()
    {
        return experience;
    }

    public List<LootEntryInfo> getLootEntryInfos()
    {
        return lootEntryInfos;
    }
}
