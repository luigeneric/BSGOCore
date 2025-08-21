package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.management.lootsystem.loot.AsteroidLoot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.sectortemplates.AsteroidDesc;
import io.github.luigeneric.templates.sectortemplates.MaxResourceDesc;
import io.github.luigeneric.templates.sectortemplates.ResourceEntry;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.ICopy;
import io.github.luigeneric.utils.ItemPicker;

public class AsteroidResourceSpawn extends ResourceSpawn implements ICopy<AsteroidResourceSpawn>
{
    private final Asteroid asteroid;
    private final MaxResourceDesc maxResourceDesc;
    private final AsteroidDesc asteroidDesc;
    private final BgoRandom bgoRandom;


    public AsteroidResourceSpawn(final SpawnController spawnController,
                                 final ItemPicker<ResourceEntry> itemPicker,
                                 final LootAssociations lootAssociations,
                                 final Asteroid asteroid,
                                 final AsteroidDesc asteroidDesc,
                                 final BgoRandom bgoRandom
    )
    {
        super(spawnController, itemPicker, asteroidDesc.respawnResourceTime(), lootAssociations);
        this.maxResourceDesc = asteroidDesc.maxResourceDesc();
        this.asteroid = asteroid;
        this.asteroidDesc = asteroidDesc;
        this.bgoRandom = bgoRandom;
    }

    public AsteroidResourceSpawn(final AsteroidResourceSpawn asteroidResourceSpawn)
    {
        this(
                asteroidResourceSpawn.spawnController,
                asteroidResourceSpawn.itemPicker,
                asteroidResourceSpawn.lootAssociations,
                asteroidResourceSpawn.asteroid,
                asteroidResourceSpawn.asteroidDesc,
                asteroidResourceSpawn.bgoRandom
        );
    }


    @Override
    public void spawn()
    {
        //check if asteroid is already dead!
        if (this.asteroid == null || this.asteroid.isRemoved())
        {
            //Log.warning("Asteroid-ResourceSpawn but asteroid is already dead!");
            return;
        }

        final int currentSpawnTime = adjustSpawnTimeIfIsInitialSpawn(this.initSpawnTime);

        final ResourceEntry rndItem = this.itemPicker.getRandomItem();
        if (rndItem.resourceType() == ResourceType.None)
        {
            this.spawnSubscriber.onSpawn(this.getNext(), currentSpawnTime);
            return;
        }

        if (asteroidDesc.maxResourceDesc().minRedPercentage() == 1)
        {
            return;
        }

        final BgoRandom rnd = itemPicker.getRnd();
        final boolean isSpawnable = rnd.rollChance(rndItem.chance());
        final AsteroidResourceDistributionRecord asteroidDistribution = spawnController.getAsteroidDistribution();
        final boolean isRedPercentOkay = isRedPercentageOkay(asteroidDistribution);
        final boolean canSpawnResource = isPercentageBelowMaxResource(rndItem, asteroidDistribution);
        if (!isSpawnable || !isRedPercentOkay || !canSpawnResource)
        {
            this.spawnSubscriber.onSpawn(this.getNext(), currentSpawnTime);
            return;
        }

        final float hpFactor = rndItem.hpToResourceFactor();

        final float maxHp = this.asteroid.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints);
        final float newResourceCount = bgoRandom.variateByPercentage((long) (maxHp * hpFactor), rndItem.variation());
        final ItemCountable newCountable = ItemCountable.fromGUID(rndItem.resourceType().guid, newResourceCount);

        this.lootAssociations.addLoot(asteroid, new AsteroidLoot(newCountable));
    }

    /**
     * Checks if the given item entry has a percentage below the maxPercentage
     * @param rndItem ResourceEntry to check
     * @param asteroidDistribution current AsteroidDistribution
     * @return true if percentage is below or equal to max percentage
     */
    private boolean isPercentageBelowMaxResource(final ResourceEntry rndItem, final AsteroidResourceDistributionRecord asteroidDistribution)
    {
        float percentage = 0;
        float maxPercentage = 0;
        switch (rndItem.resourceType())
        {
            case Tylium ->
            {
                percentage = asteroidDistribution.tyliumPercentageOfResources();
                maxPercentage = maxResourceDesc.maxTyliumPercentage();
            }
            case Titanium ->
            {
                percentage = asteroidDistribution.titaniumPercentageOfResources();
                maxPercentage = maxResourceDesc.maxTitaniumPercentage();
            }
            case Water ->
            {
                percentage = asteroidDistribution.waterPercentageOfResources();
                maxPercentage = maxResourceDesc.maxWaterPercentage();
            }
        }
        if (maxPercentage >= 100)
            return false;

        return percentage <= maxPercentage;
    }

    /**
     * Red percentage is ok
     * @param asteroidResourceDistributionRecord
     * @return true if redPercetage is higher than minRedpercentage (it's always getting lower, so min is the limit), true if asteroid count is 0
     */
    public boolean isRedPercentageOkay(final AsteroidResourceDistributionRecord asteroidResourceDistributionRecord)
    {
        final double minRedPercentage = this.maxResourceDesc.minRedPercentage();

        if (asteroidResourceDistributionRecord.asteroidCount() == 0 || asteroidResourceDistributionRecord.redCount() == 0)
            return true;


        return asteroidResourceDistributionRecord.redPercentage() > minRedPercentage;
    }

    @Override
    public SpawnAble getNext()
    {
        return this.copy();
    }

    @Override
    public AsteroidResourceSpawn copy()
    {
        return new AsteroidResourceSpawn(this);
    }
}
