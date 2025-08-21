package io.github.luigeneric.core.sector.management.lootsystem.loot;


import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.loot.LootTemplate;

import java.util.List;

public interface Loot
{
    List<LootEntryInfo> getLootItems();
    long getExp();
    boolean hasLoot();
    List<LootTemplate> getLootTemplateLst();
}

