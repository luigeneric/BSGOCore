package io.github.luigeneric.core.sector.management.lootsystem.loot;

import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.loot.LootTemplate;

import java.util.List;

/**
 * Dummy
 */
public class PvpLoot implements Loot
{
    private final LootTemplate lootTemplate;
    public PvpLoot(final LootTemplate lootTemplate)
    {
        this.lootTemplate = lootTemplate;
    }


    @Override
    public List<LootEntryInfo> getLootItems()
    {
        return this.lootTemplate.getLootEntryInfos();
    }

    @Override
    public long getExp()
    {
        return this.lootTemplate.getExperience();
    }


    @Override
    public boolean hasLoot()
    {
        return !this.lootTemplate.getLootEntryInfos().isEmpty();
    }

    @Override
    public List<LootTemplate> getLootTemplateLst()
    {
        return List.of(this.lootTemplate);
    }
}
