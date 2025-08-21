package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ObjectLeftSubscriber;
import io.github.luigeneric.core.sector.management.ScheduleItem;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.lootsystem.loot.AsteroidLoot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.sectortemplates.ResourceEntry;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.*;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.ItemPicker;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnController extends TimerQueue<SpawnAble> implements SpawnSubscriber, ObjectLeftSubscriber
{
    private final SpawnAreas areas;
    @Getter
    private final SectorDesc sectorDesc;
    @Getter
    private final Map<Long, TemplateSpaceObjectRecord> objectTemplateAssociations;
    private final ItemPicker<ResourceEntry> asteroidItemPicker;
    private final ItemPicker<ResourceEntry> planetoidItemPicker;
    @Getter
    private final Tick tick;
    private final SpaceObjectFactory spaceObjectFactory;
    private final SectorJoinQueue joinQueue;
    private final LootAssociations lootAssociations;
    private final BgoRandom random;
    private final SectorSpaceObjects spaceObjects;

    public SpawnController(final SpawnAreas areas, final SectorDesc sectorDesc,
                           final Map<Long, TemplateSpaceObjectRecord> objectTemplateAssociations,
                           final Tick tick, final SpaceObjectFactory spaceObjectFactory,
                           final SectorJoinQueue joinQueue, final LootAssociations lootAssociations,
                           final SectorSpaceObjects spaceObjects)
    {
        this.areas = areas;
        this.sectorDesc = sectorDesc;
        this.objectTemplateAssociations = objectTemplateAssociations;
        this.asteroidItemPicker = new ItemPicker<>();
        this.spaceObjectFactory = spaceObjectFactory;
        this.joinQueue = joinQueue;
        this.spaceObjects = spaceObjects;
        for (final ResourceEntry resourceEntry : sectorDesc.getAsteroidDesc().resourceEntries())
        {
            this.asteroidItemPicker.add(resourceEntry, resourceEntry.chance());
        }
        this.planetoidItemPicker = new ItemPicker<>();
        for (final ResourceEntry resourceEntry : sectorDesc.getPlanetoidDesc().resourceEntries())
        {
            this.planetoidItemPicker.add(resourceEntry, resourceEntry.chance());
        }
        this.tick = tick;
        this.lootAssociations = lootAssociations;
        this.random = new BgoRandom();
    }
    public SpawnController(final SpawnAreas areas, final SectorDesc sectorDesc, final Tick tick, final SpaceObjectFactory spaceObjectFactory,
                           final SectorJoinQueue joinQueue, final LootAssociations lootAssociations, final SectorSpaceObjects spaceObjects)
    {
        this(areas, sectorDesc, new HashMap<>(), tick, spaceObjectFactory, joinQueue, lootAssociations, spaceObjects);
    }

    public ItemPicker<ResourceEntry> getAsteroidProbabilityChooser()
    {
        return this.asteroidItemPicker;
    }


    @Override
    public void onSpawn(final SpawnAble nextSpawnable, final float spawnTime)
    {
        this.enqueue(this.tick.copy(), nextSpawnable, spawnTime);
    }

    public ItemPicker<ResourceEntry> getPlanetoidProbabilityChooser()
    {
        return planetoidItemPicker;
    }

    public SpawnAble createSpawnAble(final SpaceObjectTemplate spaceObjectTemplate) throws IllegalArgumentException
    {
        switch (spaceObjectTemplate.getSpaceEntityType())
        {
            case Asteroid ->
            {
                return new AsteroidSpawn(this, (AsteroidTemplate) spaceObjectTemplate, this.getSectorDesc(),
                        this.spaceObjectFactory,
                        this.joinQueue, random, getAsteroidProbabilityChooser(), this.lootAssociations);
            }
            case Planetoid ->
            {
                return new PlanetoidSpawn(this, (PlanetoidTemplate)spaceObjectTemplate, this.getSectorDesc(),
                        this.spaceObjectFactory,
                        this.joinQueue, getPlanetoidProbabilityChooser(), this.lootAssociations,
                        random
                );
            }
            case WeaponPlatform ->
            {
                return new WeaponPlatformSpawn(this, (WeaponPlatformTemplate)spaceObjectTemplate,
                        this.spaceObjectFactory, this.joinQueue);
            }
            case BotFighter ->
            {
                return new DynamicNpcSpawn(this, joinQueue, spaceObjectFactory, random, (BotTemplate) spaceObjectTemplate);
            }
            case Cruiser ->
            {
                return new CruiserSpawn(this, joinQueue, spaceObjectFactory, (CruiserTemplate)spaceObjectTemplate);
            }
            default ->
            {
                throw new IllegalArgumentException("Type not in factory(createSpawnAble) implemented! " + spaceObjectTemplate.getSpaceEntityType());
            }
        }
    }


    public List<ScheduleItem<SpawnAble>> getAllTimeoutItems()
    {
        return super.getAllTimeoutItems(this.getTick());
    }

    /**
     *
     * @param spawnAble
     * @param scheduleDelay time in seconds
     */
    public void enqueue(final SpawnAble spawnAble, final float scheduleDelay)
    {
        super.enqueue(this.getTick(), spawnAble, scheduleDelay);
    }

    public LootAssociations getLootAssociations()
    {
        return lootAssociations;
    }


    public AsteroidResourceDistributionRecord getAsteroidDistribution()
    {
        int countAsteroids = 0;
        int countAsteroidsWithoutResources = 0;
        int countTylium = 0;
        int countTitanium = 0;
        int countWater = 0;
        for (final TemplateSpaceObjectRecord value : this.getObjectTemplateAssociations().values())
        {
            final SpaceObject spaceObj = value.spaceObject();
            if (spaceObj.getSpaceEntityType() != SpaceEntityType.Asteroid)
                continue;

            final Asteroid tmpAsteroid = (Asteroid) spaceObj;
            countAsteroids++;

            final boolean hasLoot = lootAssociations.hasLoot(tmpAsteroid);
            final Loot loot = lootAssociations.get(tmpAsteroid).orElse(null);

            if (hasLoot)
            {
                if (loot instanceof AsteroidLoot asteroidLoot)
                {
                    final long resourceCardGUID = asteroidLoot.getRessource().getCardGuid();

                    if (resourceCardGUID == ResourceType.Tylium.guid)
                    {
                        countTylium++;
                    } else if (resourceCardGUID == ResourceType.Titanium.guid)
                    {
                        countTitanium++;
                    } else if (resourceCardGUID == ResourceType.Water.guid)
                    {
                        countWater++;
                    }
                }
            }
            else
            {
                countAsteroidsWithoutResources++;
            }
        }
        return new AsteroidResourceDistributionRecord(countAsteroids, countAsteroidsWithoutResources, countTylium, countTitanium, countWater);
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        final SpaceObject obj = arg.getRemovedSpaceObject();
        final RemovingCause removingCause = arg.getRemovingCause();

        if (obj.isPlayer())
            return;

        final TemplateSpaceObjectRecord pair = this.getObjectTemplateAssociations().remove(obj.getObjectID());
        if (pair == null)
            return;

        final SpaceObjectTemplate associatedTemplate = pair.template();
        switch (removingCause)
        {
            case Death ->
            {
                final SpawnAble spawnAble = this.createSpawnAble(associatedTemplate);
                this.enqueue(spawnAble, associatedTemplate.getRespawnTime());
            }
            case JumpOut ->
            {
                final SpawnAble spawnAble = this.createSpawnAble(associatedTemplate);
                this.enqueue(spawnAble, ((BotTemplate) associatedTemplate).getRespawnTimeJumpOut());
            }
        }
    }


}

