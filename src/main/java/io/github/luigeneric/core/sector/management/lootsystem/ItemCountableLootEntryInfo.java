package io.github.luigeneric.core.sector.management.lootsystem;


import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.shipitems.ItemCountable;

public record ItemCountableLootEntryInfo(ItemCountable itemCountable, LootEntryInfo lootEntryInfo)
{
}
