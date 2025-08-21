package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class DebrisTemplate extends SpaceObjectTemplate
{
    private final Vector3 position;
    private final Euler3 rotation;
    private final float scale;
    private final float rotationSpeed;
    public DebrisTemplate(long objectGUID, CreatingCause creatingCause,
                          int respawnTime, boolean isInstantInSector, long[] lootTemplateIds, Vector3 position, Euler3 rotation, float scale, float rotationSpeed)
    {
        super(objectGUID, SpaceEntityType.Debris, creatingCause, respawnTime, isInstantInSector, lootTemplateIds);
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.rotationSpeed = rotationSpeed;
    }

    public Vector3 getPosition()
    {
        return position;
    }

    public Euler3 getRotation()
    {
        return rotation;
    }

    public float getScale()
    {
        return scale;
    }

    public float getRotationSpeed()
    {
        return rotationSpeed;
    }

    /**
     * Retrieve a copy of the position and rotation information in the transform
     * @return a new Transform object!
     */
    public Transform getTransform()
    {
        return new Transform(this.position, this.rotation, true);
    }
}
