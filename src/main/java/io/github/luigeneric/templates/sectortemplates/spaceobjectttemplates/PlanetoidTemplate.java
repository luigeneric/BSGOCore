package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class PlanetoidTemplate extends AsteroidTemplate
{
    public PlanetoidTemplate(long objectGUID, SpaceEntityType spaceEntityType, CreatingCause creatingCause,
                             int respawnTime, boolean isInstantInSector, Vector3 position, Euler3 rotation,
                             float radius, float rotationSpeed)
    {
        super(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, position, rotation, radius,
                rotationSpeed, new long[0]);
    }
}
