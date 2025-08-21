package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;

import java.util.Objects;

public class AsteroidTemplate extends SpaceObjectTemplate
{
    protected final Vector3 position;
    protected final Euler3 rotation;
    protected final float radius;
    protected final float rotationSpeed;

    public AsteroidTemplate(long objectGUID, SpaceEntityType spaceEntityType, CreatingCause creatingCause,
                            int respawnTime, boolean isInstantInSector, Vector3 position, Euler3 rotation,
                            float radius, float rotationSpeed, final long[] lootTemplateIds)
    {
        super(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, lootTemplateIds);
        this.position = position;
        this.rotation = rotation;
        this.radius = radius;
        this.rotationSpeed = rotationSpeed;
    }
    public AsteroidTemplate(long objectGUID, SpaceEntityType spaceEntityType, CreatingCause creatingCause,
                            int respawnTime, boolean isInstantInSector, Vector3 position, Euler3 rotation,
                            float radius, float rotationSpeed)
    {
        this(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, position, rotation, radius, rotationSpeed, new long[0]);
    }

    public Vector3 getPosition()
    {
        return position;
    }

    public Euler3 getRotation()
    {
        return rotation;
    }

    public float getRadius()
    {
        return radius;
    }

    public float getRotationSpeed()
    {
        return rotationSpeed;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AsteroidTemplate that = (AsteroidTemplate) o;

        if (Float.compare(that.radius, radius) != 0) return false;
        if (Float.compare(that.rotationSpeed, rotationSpeed) != 0) return false;
        if (!Objects.equals(position, that.position)) return false;
        return Objects.equals(rotation, that.rotation);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (rotation != null ? rotation.hashCode() : 0);
        result = 31 * result + (radius != +0.0f ? Float.floatToIntBits(radius) : 0);
        result = 31 * result + (rotationSpeed != +0.0f ? Float.floatToIntBits(rotationSpeed) : 0);
        return result;
    }

    public Transform getTransform()
    {
        return new Transform(this.position, this.rotation, true);
    }
}
