package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.intersection.IntersectionAlgorithms;

public class SphereCollider extends Collider
{
    private final Sphere sphere;
    private final Vector3 updatedCenter;

    public SphereCollider(final Transform transform, final Vector3 center, final float radius)
    {
        super(transform, radius);
        this.sphere = new Sphere(center, radius);
        this.updatedCenter = center.copy();
        this.updatePositions();
    }
    public Vector3 getCenter()
    {
        return this.updatedCenter;
    }

    public float getRadius()
    {
        return this.sphere.radius();
    }


    @Override
    public void updatePositions()
    {
        //rotation update is not needed since its a sphere and rotation is not important
        this.updatedCenter.set(Vector3.add(this.sphere.center(), this.transform.getPosition()));
    }

    @Override
    public CollisionRecord collides(Collider collider)
    {
        return collider.collides(this);
    }

    @Override
    public CollisionRecord collides(SphereCollider sphereCollider)
    {
        return IntersectionAlgorithms.testSphereSphere(this, sphereCollider);
    }

    @Override
    public CollisionRecord collides(OBBCollider obbCollider)
    {
        return IntersectionAlgorithms.testSphereOBB(this, obbCollider, false);
    }

    @Override
    public CollisionRecord collides(CapsuleCollider capsuleCollider)
    {
        return IntersectionAlgorithms.testSphereCapsule(this, capsuleCollider, false);
    }

    @Override
    public boolean collidesPrimitive(final Collider collider)
    {
        return collider.collidesPrimitive(this);
    }

    @Override
    public boolean collidesPrimitive(final SphereCollider sphereCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectSphereSphere(this, sphereCollider);
    }

    @Override
    public boolean collidesPrimitive(final OBBCollider obbCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectSphereOBB(this, obbCollider, new Vector3());
    }

    @Override
    public boolean collidesPrimitive(final CapsuleCollider capsuleCollider)
    {
        return PrimitiveIntersectionAlgorithms.testSphereCapsulePrimitive(this, capsuleCollider);
    }

    @Override
    public Collider copy()
    {
        return new SphereCollider(this.transform.copy(), this.sphere.center().copy(), this.sphere.radius());
    }
}
