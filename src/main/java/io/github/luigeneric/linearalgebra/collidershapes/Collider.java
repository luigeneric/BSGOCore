package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.utils.ICopy;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class Collider implements ICopy<Collider>
{
    protected final Transform transform;
    protected final float pruneSphereRadius;

    public Collider(final Transform transform, final float pruneSphereRadius)
    {
        this.transform = transform;
        this.pruneSphereRadius = pruneSphereRadius;
    }

    public abstract void updatePositions();

    public abstract CollisionRecord collides(final Collider collider);
    public abstract CollisionRecord collides(final SphereCollider sphereCollider);
    public abstract CollisionRecord collides(final OBBCollider obbCollider);
    public abstract CollisionRecord collides(final CapsuleCollider capsuleCollider);

    public abstract boolean collidesPrimitive(final Collider collider);
    public abstract boolean collidesPrimitive(final SphereCollider sphereCollider);
    public abstract boolean collidesPrimitive(final OBBCollider obbCollider);
    public abstract boolean collidesPrimitive(final CapsuleCollider capsuleCollider);
}