package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.templates.sectortemplates.ResourceEntry;
import io.github.luigeneric.utils.ItemPicker;

public abstract class ResourceSpawn extends SpawnAble
{
    protected final ItemPicker<ResourceEntry> itemPicker;
    protected final int initSpawnTime;
    protected final LootAssociations lootAssociations;

    public ResourceSpawn(final SpawnController spawnController, final ItemPicker<ResourceEntry> itemPicker,
                         final int initSpawnTime, LootAssociations lootAssociations)
    {
        super(spawnController, spawnController);
        this.itemPicker = itemPicker;
        this.initSpawnTime = initSpawnTime;
        this.lootAssociations = lootAssociations;
    }
}
