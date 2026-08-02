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
    private final long exp;

    public AsteroidLoot(final ItemCountable ressource)
    {
        this(ressource, 50);
    }

    public AsteroidLoot(final ItemCountable ressource, final long exp)
    {
        this.ressource = ressource;
        this.exp = exp;
    }

    /**
     * XP for cracking a rock, scaled by how much rock there was.
     * A flat 50 made the safe home sectors (200-400 HP) pay ~3.7x the XP/hour of contested
     * Tannhauser (600-1600 HP), because the only thing that changes with HP is time-to-kill.
     * @param maxHp the asteroid's MaxHullPoints
     */
    public static long expForHp(final float maxHp)
    {
        return Math.max(5L, (long) (maxHp / 20f));
    }


    @Override
    public List<LootEntryInfo> getLootItems()
    {
        return List.of(new LootEntryInfo(1, new short[]{0, 255}, ressource, 0));
    }

    @Override
    public long getExp()
    {
        return this.exp;
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
