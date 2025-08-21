package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Mathf;

/**
 * @param normal      normal vector to the collision contact in worldspace
 * @param localPoint1 contact point of body1 in local space of body 1
 * @param localPoint2 contact point of body2 in local space of body 2
 */
public record CollisionRecord(boolean collides, float penetrationDepth, Vector3 normal, Vector3 localPoint1, Vector3 localPoint2)
{
    private final static CollisionRecord NO_COLLISION = new CollisionRecord(false, 0, null, null, null);

    public CollisionRecord noCollision()
    {
        return NO_COLLISION;
    }

    public CollisionRecord
    {
        penetrationDepth = Mathf.abs(penetrationDepth);
    }

    public CollisionRecord(final float penetrationDepth, final Vector3 normal, final Vector3 localPoint1, final Vector3 localPoint2)
    {
        this(true, penetrationDepth, normal, localPoint1, localPoint2);
    }
    public CollisionRecord(final float penetrationDepth, final Vector3 normal)
    {
        this(true, penetrationDepth, normal, null, null);
    }
}
