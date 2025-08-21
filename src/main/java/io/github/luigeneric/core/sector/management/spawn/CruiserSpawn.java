package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.spaceentities.CruiserShip;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.CruiserTemplate;

public class CruiserSpawn extends SpawnAble
{
    private final SectorJoinQueue joinQueue;
    private final SpaceObjectFactory factory;
    private final CruiserTemplate cruiserTemplate;

    public CruiserSpawn(final SpawnController spawnController,
                        final SectorJoinQueue joinQueue,
                        final SpaceObjectFactory factory,
                        final CruiserTemplate cruiserTemplate
                        )
    {
        super(spawnController, spawnController);
        this.joinQueue = joinQueue;
        this.factory = factory;
        this.cruiserTemplate = cruiserTemplate;
    }

    @Override
    public void spawn()
    {
        final CruiserShip tmpCruiser = factory.createCruiser(cruiserTemplate);
        joinQueue.addSpaceObject(tmpCruiser);
    }

    @Override
    public SpawnAble getNext()
    {
        return null;
    }
}
