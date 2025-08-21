package io.github.luigeneric.templates.colliderstemplates;


import io.github.luigeneric.linearalgebra.base.Vector3;

public class CapsuleColliderTemplate extends ColliderTemplate
{
    private final Vector3 center;

    /**
     * 0=x
     * 1=y
     * 2=z
     */
    private final int axis;
    private final float height;
    private final float radius;

    public CapsuleColliderTemplate(String prefabName, Vector3 center, int axis, float height, float radius)
    {
        super(prefabName, ColliderType.Capsule);
        this.center = center;
        this.axis = axis;
        this.height = height;
        this.radius = radius;
    }

    public Vector3 getCenter()
    {
        return center;
    }

    public int getAxis()
    {
        return axis;
    }

    public float getHeight()
    {
        return height;
    }

    public float getRadius()
    {
        return radius;
    }
}
