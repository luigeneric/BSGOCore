package io.github.luigeneric.templates.colliderstemplates;


import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.CapsuleCollider;
import io.github.luigeneric.linearalgebra.collidershapes.Collider;
import io.github.luigeneric.linearalgebra.collidershapes.OBBCollider;
import io.github.luigeneric.linearalgebra.collidershapes.SphereCollider;

public class ColliderFactory
{
    public static Collider fromTemplate(final ColliderTemplate colliderTemplate, final Transform transformRef, final float scale)
    {
        if (colliderTemplate instanceof SphereColliderTemplate sphereColliderTemplate)
        {
            return fromTemplate(sphereColliderTemplate, transformRef);
        }
        else if (colliderTemplate instanceof CapsuleColliderTemplate cap)
        {
            return fromTemplate(cap, transformRef, scale);
        }
        else if (colliderTemplate instanceof AABBColliderTemplate a)
        {
            return fromTemplate(a, transformRef);
        }
        throw new IllegalStateException("Could not find ColliderTemplate instance");
    }
    public static Collider fromTemplate(final ColliderTemplate colliderTemplate, final Transform transformRef)
    {
        return fromTemplate(colliderTemplate, transformRef, 1);
    }


    public static SphereCollider fromTemplate(final SphereColliderTemplate sphereColliderTemplate, final Transform transformRef)
    {
        return new SphereCollider(transformRef, sphereColliderTemplate.getCenter(), sphereColliderTemplate.getRadius());
    }

    public static CapsuleCollider fromTemplate(final CapsuleColliderTemplate capsuleColliderTemplate, final Transform transformRef, final float scale)
    {
        final int axisDirection = capsuleColliderTemplate.getAxis();

        final float x = axisDirection == 0 ? 1 : 0;
        final float y = axisDirection == 1 ? 1 : 0;
        final float z = axisDirection == 2 ? 1 : 0;

        final Vector3 axisDummy = new Vector3(x, y, z);
        final float halfHeight = capsuleColliderTemplate.getHeight() * 0.5f * scale;
        final float radius = capsuleColliderTemplate.getRadius() * scale;
        final float halfHeightMinRadius = halfHeight - radius;
        final Vector3 center = capsuleColliderTemplate.getCenter().copy().scale(scale);


        final Vector3 a = Vector3.add(center, Vector3.mult(axisDummy, halfHeightMinRadius));
        final Vector3 b = Vector3.sub(center, Vector3.mult(axisDummy, halfHeightMinRadius));


        return new CapsuleCollider(transformRef, a, b , radius);
    }

    public static OBBCollider fromTemplate(final AABBColliderTemplate c, final Transform transformRef)
    {
        //calculate min and max

        return new OBBCollider(transformRef, c.getCenter(), c.getExtents());
    }
}
