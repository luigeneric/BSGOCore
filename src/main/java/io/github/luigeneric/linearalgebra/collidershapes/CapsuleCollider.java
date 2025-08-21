package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.intersection.IntersectionAlgorithms;
import io.github.luigeneric.linearalgebra.utility.FloatWrapper;

import java.util.Objects;

public class CapsuleCollider extends Collider
{
    private final Capsule capsule;
    private final Vector3 updatedA;
    private final Vector3 updatedB;

    public CapsuleCollider(final Transform transform, final Vector3 a, final Vector3 b,
                           final float radius)
    {
        super(transform, Vector3.distance(a, b) * 0.5f + radius);

        Objects.requireNonNull(a, "Vec cannot be null");
        Objects.requireNonNull(b, "Vec cannot be null");

        this.capsule = new Capsule(a, b, radius);

        this.updatedA = a.copy();
        this.updatedB = b.copy();
        this.updatePositions();
    }


    public Vector3 getA()
    {
        return updatedA;
    }

    public Vector3 getB()
    {
        return updatedB;
    }

    public float getRadius()
    {
        return capsule.radius();
    }

    @Override
    public void updatePositions()
    {
        this.updatedA.set(this.transform.applyTransform(this.capsule.a()));
        this.updatedB.set(this.transform.applyTransform(this.capsule.b()));
    }

    @Override
    public CollisionRecord collides(final Collider collider)
    {
        return collider.collides(this);
    }

    @Override
    public CollisionRecord collides(final SphereCollider sphereCollider)
    {
        return IntersectionAlgorithms.testSphereCapsule(sphereCollider, this, true);
    }

    @Override
    public CollisionRecord collides(final OBBCollider obbCollider)
    {
        return IntersectionAlgorithms.testCapsuleObb3(this, obbCollider, false);
        //return IntersectionAlgorithms.testCapsuleObbAI2(this, obbCollider, false);
        //return IntersectionAlgorithms.testCapsuleObb2(this, obbCollider, false);
        //return PrimitiveIntersectionAlgorithms.testCapsuleObb(this, obbCollider, false);
    }

    @Override
    public CollisionRecord collides(final CapsuleCollider capsuleCollider)
    {
        return IntersectionAlgorithms.testCapsuleCapsule(this, capsuleCollider);
    }

    @Override
    public boolean collidesPrimitive(final Collider collider)
    {
        return collider.collidesPrimitive(this);
    }

    @Override
    public boolean collidesPrimitive(final SphereCollider sphereCollider)
    {
        return PrimitiveIntersectionAlgorithms.testSphereCapsulePrimitive(sphereCollider, this);
    }

    @Override
    public boolean collidesPrimitive(final OBBCollider obbCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectCapsuleOBB(this, obbCollider, new FloatWrapper());
    }

    @Override
    public boolean collidesPrimitive(final CapsuleCollider capsuleCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectCapsuleCapsule(this, capsuleCollider);
    }

    @Override
    public Collider copy()
    {
        return new CapsuleCollider(this.transform.copy(), this.updatedA.copy(), this.updatedB.copy(), this.getRadius());
    }
}
