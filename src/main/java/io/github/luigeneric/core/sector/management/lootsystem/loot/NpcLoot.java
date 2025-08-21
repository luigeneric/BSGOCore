package io.github.luigeneric.core.sector.management.lootsystem.loot;


import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.loot.LootTemplate;

import java.util.List;

public class NpcLoot implements Loot
{
    private final List<LootTemplate> lootTemplates;

    public NpcLoot(final List<LootTemplate> lootTemplates)
    {
        this.lootTemplates = lootTemplates;
    }

    @Override
    public List<LootEntryInfo> getLootItems()
    {
        return lootTemplates.stream()
                .map(LootTemplate::getLootEntryInfos)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public long getExp()
    {
        return this.lootTemplates.stream()
                .map(LootTemplate::getExperience)
                .mapToLong(v -> v)
                .sum();
    }

    @Override
    public boolean hasLoot()
    {
        return !this.lootTemplates.isEmpty();
    }

    @Override
    public List<LootTemplate> getLootTemplateLst()
    {
        return this.lootTemplates;
    }
}
