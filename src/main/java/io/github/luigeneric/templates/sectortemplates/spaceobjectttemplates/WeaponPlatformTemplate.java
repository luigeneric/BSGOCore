package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class WeaponPlatformTemplate extends StaticNpcTemplate
{
    public WeaponPlatformTemplate(final long objectGUID, final SpaceEntityType spaceEntityType, final CreatingCause creatingCause, final int respawnTime,
                                  final boolean isInstantInSector, final Vector3 position, final Euler3 rotation,
                                  final float autoAggroDistance, final float maximumChaseDistance, Faction faction, final long[] lootTemplateId)
    {
        super(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, position, rotation,
                autoAggroDistance, maximumChaseDistance, faction, lootTemplateId);
    }
}
