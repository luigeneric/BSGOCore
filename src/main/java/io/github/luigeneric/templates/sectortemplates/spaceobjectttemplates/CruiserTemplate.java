package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class CruiserTemplate extends StaticNpcTemplate
{
    public CruiserTemplate(long objectGUID, SpaceEntityType spaceEntityType, CreatingCause creatingCause,
                           int respawnTime, boolean isInstantInSector, Vector3 position, Euler3 rotation,
                           float autoAggroDistance, float maximumAggroDistance, Faction faction, long[] lootTemplateIds)
    {
        super(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, position, rotation, autoAggroDistance, maximumAggroDistance, faction, lootTemplateIds);
    }
}
