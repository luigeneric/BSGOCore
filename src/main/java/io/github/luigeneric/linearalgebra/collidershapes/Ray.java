package io.github.luigeneric.linearalgebra.collidershapes;

import io.github.luigeneric.linearalgebra.base.Vector3;

public record Ray(Vector3 origin, Vector3 direction, float travelDistance)
{
    public Ray(final Vector3 origin, final Vector3 direction, float travelDistance)
    {
        this.origin = origin.copy();
        this.direction = direction;
        this.travelDistance = travelDistance;
    }

    public static Ray fromLineSegment(final Vector3 a, final Vector3 b)
    {
        final Vector3 d = Vector3.sub(b, a);
        return new Ray(a, d, 1.0f);
    }

    /**
     * Calculates a point at distance units along the ray
     *
     * @param distance
     * @return a point at distance units along the ray
     */
    public Vector3 getPoint(final float distance)
    {
        return Vector3.add(this.origin, Vector3.mult(this.direction, distance));
    }

    public static Ray fromLineSegment(final LineSegment lineSegment)
    {
        return Ray.fromLineSegment(lineSegment.a(), lineSegment.b());
    }


    @Override
    public String toString()
    {
        return "Ray{" +
                "origin=" + origin +
                ", direction=" + direction +
                ", length=" + travelDistance +
                '}';
    }
}
