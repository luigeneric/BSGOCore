package io.github.luigeneric.templates.colliderstemplates;


import io.github.luigeneric.linearalgebra.base.Vector3;

public class SphereColliderTemplate extends ColliderTemplate
{
    private final Vector3 center;
    private final float radius;
    public SphereColliderTemplate(final Vector3 center, final float radius)
    {
        super("asteroid", ColliderType.Sphere);
        this.center = center;
        this.radius = radius;
    }

    public float getRadius()
    {
        return radius;
    }

    public Vector3 getCenter()
    {
        return center;
    }
}
