package io.github.luigeneric.core.sector.management.spawn;

import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.templates.catalogue.WorldOwnerCard;
import io.github.luigeneric.templates.sectortemplates.PlanetoidDesc;
import io.github.luigeneric.templates.sectortemplates.ResourceEntry;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.PlanetoidTemplate;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.ItemPicker;

import java.util.concurrent.TimeUnit;

public class PlanetoidSpawn extends SpawnAble
{
    private final PlanetoidTemplate planetoidTemplate;
    private final PlanetoidDesc planetoidDesc;
    private final SpaceObjectFactory spaceObjectFactory;
    private final SectorJoinQueue joinQueue;
    private final ItemPicker<ResourceEntry> itemPicker;
    private Planetoid planetoid;
    private final LootAssociations lootAssociations;
    private final BgoRandom bgoRandom;
    public PlanetoidSpawn(final SpawnController spawnController,
                          final PlanetoidTemplate planetoidTemplate,
                          final SectorDesc sectorDesc,
                          final SpaceObjectFactory spaceObjectFactory,
                          final SectorJoinQueue joinQueue,
                          final ItemPicker<ResourceEntry> itemPicker,
                          final LootAssociations lootAssociations,
                          final BgoRandom bgoRandom
    )
    {
        super(spawnController, spawnController);
        this.planetoidTemplate = planetoidTemplate;
        this.planetoidDesc = sectorDesc.getPlanetoidDesc();
        this.spaceObjectFactory = spaceObjectFactory;
        this.joinQueue = joinQueue;
        this.itemPicker = itemPicker;
        this.lootAssociations = lootAssociations;
        this.bgoRandom = bgoRandom;
    }

    @Override
    public void spawn()
    {
        final long guid = this.planetoidTemplate.getObjectGUID();

        final WorldOwnerCard worldOwnerCards = catalogue.fetchWorldOwnerCards(guid);

        final Planetoid planetoid = this.spaceObjectFactory.createPlanetoid(this.planetoidTemplate);
        planetoid.createMovementController(planetoidTemplate.getTransform());


        this.joinQueue.addSpaceObject(planetoid);

        this.planetoid = planetoid;

        final boolean isServerJustRestarted = spawnController.getTick().getTimePassedSinceStart(TimeUnit.MINUTES) < 1;
        final int respawnResourceTime = isServerJustRestarted ?
                1 : this.planetoidDesc.respawnResourceTime();


        this.spawnSubscriber.onSpawn(this.getNext(), respawnResourceTime);
        this.spawnController.getObjectTemplateAssociations().put(planetoid.getObjectID(),
                new TemplateSpaceObjectRecord(planetoidTemplate, planetoid));
    }

    @Override
    public SpawnAble getNext()
    {
        return new PlanetoidResourceSpawn(
                this.spawnController,
                this.itemPicker,
                this.planetoidDesc,
                this.lootAssociations,
                this.planetoid,
                this.bgoRandom
        );
    }
}
