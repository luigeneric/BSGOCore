package io.github.luigeneric.linearalgebra.collidershapes;

import io.github.luigeneric.linearalgebra.base.UnmodifiableDecorator;
import io.github.luigeneric.linearalgebra.base.Vector3;

public record Sphere(Vector3 center, float radius)
{

    @Override
    public Vector3 center()
    {
        return UnmodifiableDecorator.wrap(this.center);
    }

    public boolean isInsideSphere(final Vector3 position)
    {
        return PrimitiveIntersectionAlgorithms.intersectSphereSphere(center, radius, position, 0);
    }
}
