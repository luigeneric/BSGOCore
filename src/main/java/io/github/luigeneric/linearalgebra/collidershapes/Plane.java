package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Vector3;

public class Plane
{
    /**
     * Plane normal. Points x on the plane satisfy Dot(n,x) = d
     */
    private final Vector3 n;
    /**
     * d = dot(n,p) for a given point p on the plane
     */
    private final float d;

    public Plane(final Vector3 a, final Vector3 b, final Vector3 c)
    {
        this.n = Vector3.normalize(
                Vector3.cross(
                        Vector3.sub(b, a),
                        Vector3.sub(c, a))
        );
        this.d = Vector3.dot(this.n, a);
    }
    public Plane(final Vector3 n, final float d)
    {
        this.n = n;
        this.d = d;
    }

    public Vector3 getN()
    {
        return n;
    }

    public float getD()
    {
        return d;
    }
}
