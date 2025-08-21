package io.github.luigeneric.templates.colliderstemplates;


import io.github.luigeneric.linearalgebra.base.Vector3;

public class AABBColliderTemplate extends ColliderTemplate
{
    private final Vector3 center;
    private final Vector3 extents;
    public AABBColliderTemplate(final String prefabName, Vector3 center, Vector3 extents)
    {
        super(prefabName, ColliderType.AABB);
        this.center = center;
        this.extents = extents;
    }


    public Vector3 getCenter()
    {
        return center;
    }

    public Vector3 getExtents()
    {
        return extents;
    }
}
