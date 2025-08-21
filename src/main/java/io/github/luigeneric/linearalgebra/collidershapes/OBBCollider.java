package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.StaticVectors;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.intersection.IntersectionAlgorithms;
import io.github.luigeneric.linearalgebra.intersection.OOBBIntersectionAlgorithm;
import io.github.luigeneric.linearalgebra.utility.FloatWrapper;
import io.github.luigeneric.linearalgebra.utility.Mathf;

public class OBBCollider extends Collider
{
    private final Obb obb;
    private final Vector3 updatedCenter;
    private final Vector3 localRight;
    private final Vector3 localUp;
    private final Vector3 localForward;


    public OBBCollider(final Transform transform, final Vector3 center, final Vector3 halfWidthExtents)
    {
        super(transform, Mathf.max(halfWidthExtents.getX(), halfWidthExtents.getY(), halfWidthExtents.getZ()));
        this.obb = new Obb(center, halfWidthExtents);
        this.updatedCenter = center.copy();
        this.localRight = Vector3.right();
        this.localUp = Vector3.up();
        this.localForward = Vector3.forward();
        this.updatePositions();
    }

    @Override
    public void updatePositions()
    {
        this.updatedCenter.set(Vector3.add(this.transform.getPosition(), this.obb.center()));

        this.localRight.set(this.transform.getRotation().mult(StaticVectors.RIGHT));
        this.localUp.set(this.transform.getRotation().mult(StaticVectors.UP));
        this.localForward.set(this.transform.getRotation().mult(StaticVectors.FORWARD));
    }

    public Vector3 getLocalAxes(final int index)
    {
        return switch (index)
        {
            case 0 -> localRight;
            case 1 -> localUp;
            case 2 -> localForward;
            default -> throw new IllegalArgumentException("Invalid index");
        };
    }

    public float getMaximumRadius()
    {
        return this.pruneSphereRadius;
    }

    @Override
    public CollisionRecord collides(final Collider collider)
    {
        return collider.collides(this);
    }

    @Override
    public CollisionRecord collides(final SphereCollider sphereCollider)
    {
        return IntersectionAlgorithms.testSphereOBB(sphereCollider, this, true);
    }

    @Override
    public CollisionRecord collides(final OBBCollider obbCollider)
    {
        //final boolean intersection = PrimitiveIntersectionAlgorithms.intersectOBBOBB(this, obbCollider);
        //if (intersection)
        //    return IntersectionAlgorithms.testOBBOBB(this, obbCollider);
        //return null;
        return OOBBIntersectionAlgorithm.checkForCollision(this, obbCollider);
    }


    @Override
    public CollisionRecord collides(final CapsuleCollider capsuleCollider)
    {
        return IntersectionAlgorithms.testCapsuleObb3(capsuleCollider, this, true);
        //return IntersectionAlgorithms.testCapsuleObbAI2(capsuleCollider, this, true);
    }

    @Override
    public boolean collidesPrimitive(final Collider collider)
    {
        return collider.collidesPrimitive(this);
    }

    @Override
    public boolean collidesPrimitive(final SphereCollider sphereCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectSphereOBB(sphereCollider, this, new Vector3());
    }

    @Override
    public boolean collidesPrimitive(final OBBCollider obbCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectOBBOBB(this, obbCollider);
    }

    @Override
    public boolean collidesPrimitive(final CapsuleCollider capsuleCollider)
    {
        return PrimitiveIntersectionAlgorithms.intersectCapsuleOBB(capsuleCollider, this, new FloatWrapper());
    }

    public Vector3 getGlobalCenter()
    {
        return updatedCenter;
    }

    public Vector3 getHalfWidthExtents()
    {
        return this.obb.halfWidthExtents();
    }

    public Vector3 getLocalRight()
    {
        return localRight;
    }

    public Vector3 getLocalUp()
    {
        return localUp;
    }

    public Vector3 getLocalForward()
    {
        return localForward;
    }

    public Vector3 getMinPrimitive()
    {
        return Vector3.sub(this.getGlobalCenter(), this.getHalfWidthExtents());
    }
    public Vector3 getMin()
    {
        //return Vector3.sub(this.updatedCenter, this.halfWidthExtents);
        return Vector3.sub(this.getGlobalCenter(), this.getLocalHalfWidthComputed());
    }
    public Vector3 getMaxPrimitive()
    {
        return Vector3.add(this.getGlobalCenter(), this.getHalfWidthExtents());
    }

    public Vector3 getMax()
    {
        //return Vector3.add(this.updatedCenter, this.halfWidthExtents);
        return Vector3.add(this.getGlobalCenter(), this.getLocalHalfWidthComputed());
    }
    public Vector3 getLocalHalfWidthComputed()
    {
        return new Vector3(
                //x
                Vector3.dot(this.getHalfWidthExtents(), this.getLocalRight()),
                //y
                Vector3.dot(this.getHalfWidthExtents(), this.getLocalUp()),
                //z
                Vector3.dot(this.getHalfWidthExtents(), this.getLocalForward())
        );
    }

    @Override
    public Collider copy()
    {
        return new OBBCollider(this.transform.copy(), this.updatedCenter.copy(), this.obb.halfWidthExtents().copy());
    }
}
