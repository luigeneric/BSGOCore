package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.Color;

public class PlanetTemplate extends SpaceObjectTemplate
{
    private final Vector3 position;
    private final Euler3 rotation;
    private final float scale;
    private final Color color;
    private final Color specularColor;
    private final float shininess;

    public PlanetTemplate(long objectGUID, CreatingCause creatingCause, int respawnTime, boolean isInstantInSector,
                          Vector3 position, Euler3 rotation, float scale, Color color, Color specularColor, float shininess)
    {
        super(objectGUID, SpaceEntityType.Planet, creatingCause, respawnTime, isInstantInSector, new long[0]);
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.color = color;
        this.specularColor = specularColor;
        this.shininess = shininess;
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

    public Color getColor()
    {
        return color;
    }

    public Color getSpecularColor()
    {
        return specularColor;
    }

    public float getShininess()
    {
        return shininess;
    }

    public Transform getTransform()
    {
        return new Transform(position, rotation, true);
    }
}
