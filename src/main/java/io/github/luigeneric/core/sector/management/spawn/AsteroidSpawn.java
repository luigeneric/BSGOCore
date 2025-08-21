package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.templates.sectortemplates.AsteroidDesc;
import io.github.luigeneric.templates.sectortemplates.ResourceEntry;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.AsteroidTemplate;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.ItemPicker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsteroidSpawn extends SpawnAble
{
    protected final BgoRandom bgoRandom;
    protected final AsteroidDesc asteroidDesc;
    protected final AsteroidTemplate asteroidTemplate;
    //protected final ObjectIDRegistry spaceObjectFactory;
    protected final SpaceObjectFactory spaceObjectFactory;
    protected final SectorJoinQueue joinQueue;
    private Asteroid asteroid;
    private final ItemPicker<ResourceEntry> itemPicker;
    private final LootAssociations lootAssociations;

    public AsteroidSpawn(final SpawnController spawnController, final AsteroidTemplate asteroidTemplate, final SectorDesc sectorDesc,
                         final SpaceObjectFactory spaceObjectFactory, final SectorJoinQueue joinQueue, final BgoRandom random,
                         final ItemPicker<ResourceEntry> itemPicker, final LootAssociations lootAssociations)
    {
        super(spawnController, spawnController);
        this.asteroidTemplate = asteroidTemplate;
        this.asteroidDesc = sectorDesc.getAsteroidDesc();
        this.spaceObjectFactory = spaceObjectFactory;
        this.joinQueue = joinQueue;

        this.bgoRandom = random;
        this.itemPicker = itemPicker;
        this.lootAssociations = lootAssociations;
    }

    @Override
    public void spawn()
    {
        final float hp = calculateHp();
        final Asteroid asteroid = this.spaceObjectFactory.createAsteroid(asteroidTemplate, hp);

        this.joinQueue.addSpaceObject(asteroid);
        this.asteroid = asteroid;


        final int respawnResourceTime = adjustSpawnTimeIfIsInitialSpawn(asteroidDesc.respawnResourceTime());

        this.spawnSubscriber.onSpawn(this.getNext(), respawnResourceTime);
        this.spawnController.getObjectTemplateAssociations().put(asteroid.getObjectID(),
                new TemplateSpaceObjectRecord(asteroidTemplate, asteroid));
    }

    @Override
    public SpawnAble getNext()
    {
        return new AsteroidResourceSpawn(this.spawnController, itemPicker, lootAssociations, asteroid, asteroidDesc, bgoRandom);
    }

    private float calculateHp()
    {
        float hp = 1;
        try
        {
            final int min = this.asteroidDesc.hpIntervall()[0];
            final int max = this.asteroidDesc.hpIntervall()[1];

            hp = this.bgoRandom.getRndBetweenInt(min, max);
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException)
        {
            log.error(indexOutOfBoundsException.getMessage());
        }

        return hp;
    }
}
