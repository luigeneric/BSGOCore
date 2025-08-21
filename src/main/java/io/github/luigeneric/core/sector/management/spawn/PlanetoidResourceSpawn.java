package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.management.lootsystem.loot.AsteroidLoot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.sectortemplates.PlanetoidDesc;
import io.github.luigeneric.templates.sectortemplates.ResourceEntry;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.ItemPicker;

public class PlanetoidResourceSpawn extends ResourceSpawn
{
    private final PlanetoidDesc planetoidDesc;
    private final Planetoid planetoid;
    private final BgoRandom bgoRandom;
    public PlanetoidResourceSpawn(final SpawnController spawnController,
                                  final ItemPicker<ResourceEntry> itemPicker,
                                  final PlanetoidDesc planetoidDesc,
                                  final LootAssociations lootAssociations,
                                  final Planetoid planetoid,
                                  final BgoRandom bgoRandom
    )
    {
        super(spawnController, itemPicker, planetoidDesc.respawnResourceTime(), lootAssociations);
        this.planetoidDesc = planetoidDesc;
        this.planetoid = planetoid;
        this.bgoRandom = bgoRandom;
    }

    public PlanetoidResourceSpawn(final PlanetoidResourceSpawn planetoidResourceSpawn)
    {
        this(planetoidResourceSpawn.spawnController,
                planetoidResourceSpawn.itemPicker,
                planetoidResourceSpawn.planetoidDesc,
                planetoidResourceSpawn.lootAssociations,
                planetoidResourceSpawn.planetoid,
                planetoidResourceSpawn.bgoRandom
        );
    }

    @Override
    public void spawn()
    {
        if (this.planetoid == null || this.planetoid.isRemoved())
        {
            return;
        }

        final int currentSpawnTime = adjustSpawnTimeIfIsInitialSpawn(this.initSpawnTime);

        final ResourceEntry rndItem = this.itemPicker.getRandomItem();
        if (rndItem.resourceType() == ResourceType.None)
        {
            this.spawnSubscriber.onSpawn(this.getNext(), currentSpawnTime);
            return;
        }
        final boolean isSpawnable = bgoRandom.rollChance(rndItem.chance());
        if (!isSpawnable)
        {
            this.spawnSubscriber.onSpawn(this.getNext(), currentSpawnTime);
            return;
        }

        final int min = this.planetoidDesc.minResources();
        final int max = this.planetoidDesc.maxResources();
        final int value = bgoRandom.getRndBetweenInt(min, max);

        this.lootAssociations.addLoot(planetoid, new AsteroidLoot(ItemCountable.fromGUID(rndItem.resourceType(), value)));
    }

    @Override
    public SpawnAble getNext()
    {
        return this.copy();
    }

    private PlanetoidResourceSpawn copy()
    {
        return new PlanetoidResourceSpawn(this);
    }
}
