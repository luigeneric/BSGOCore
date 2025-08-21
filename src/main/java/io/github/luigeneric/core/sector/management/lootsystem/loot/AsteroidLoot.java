package io.github.luigeneric.core.sector.management.lootsystem.loot;


import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.loot.LootDamageTemplate;
import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.loot.LootTemplate;
import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.List;

public class AsteroidLoot implements Loot
{
    private final ItemCountable ressource;

    public AsteroidLoot(final ItemCountable ressource)
    {
        this.ressource = ressource;
    }


    @Override
    public List<LootEntryInfo> getLootItems()
    {
        return List.of(new LootEntryInfo(1, new short[]{0, 255}, ressource, 0));
    }

    @Override
    public long getExp()
    {
        return 50;
    }

    @Override
    public boolean hasLoot()
    {
        return this.ressource != null && this.ressource.getCardGuid() != ResourceType.None.guid;
    }

    @Override
    public List<LootTemplate> getLootTemplateLst()
    {
        return List.of(LootDamageTemplate.ofSingle(this.ressource));
    }

    public ItemCountable getRessource()
    {
        return ressource;
    }
}
