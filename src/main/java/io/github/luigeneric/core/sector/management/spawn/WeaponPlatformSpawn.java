package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.WeaponPlatformTemplate;

public class WeaponPlatformSpawn extends SpawnAble
{
    private final WeaponPlatformTemplate weaponPlatformTemplate;
    private final SpaceObjectFactory spaceObjectFactory;
    final SectorJoinQueue joinQueue;
    public WeaponPlatformSpawn(final SpawnController spawnController, final WeaponPlatformTemplate spaceObjectTemplate,
                               final SpaceObjectFactory spaceObjectFactory, final SectorJoinQueue joinQueue)
    {
        super(spawnController, spawnController);
        this.weaponPlatformTemplate = spaceObjectTemplate;
        this.spaceObjectFactory = spaceObjectFactory;
        this.joinQueue = joinQueue;
    }

    @Override
    public void spawn()
    {
        final SpaceObject newPlatform = this.spaceObjectFactory.createWeaponPlatform(weaponPlatformTemplate);
        this.joinQueue.addSpaceObject(newPlatform);
        this.spawnController.getObjectTemplateAssociations().put(newPlatform.getObjectID(),
                new TemplateSpaceObjectRecord(weaponPlatformTemplate, newPlatform));
    }

    @Override
    public SpawnAble getNext()
    {
        return null;
    }
}
