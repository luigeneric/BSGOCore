package io.github.luigeneric.linearalgebra.collidershapes;

import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.ICopy;

public class Triangle implements ICopy<Triangle>
{
    private final Vector3 a;
    private final Vector3 b;
    private final Vector3 c;


    public Triangle(Vector3 a, Vector3 b, Vector3 c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }


    @Override
    public Triangle copy()
    {
        return new Triangle(this.a.copy(), this.b.copy(), this.c.copy());
    }


    public Vector3 getA()
    {
        return a;
    }

    public Vector3 getB()
    {
        return b;
    }

    public Vector3 getC()
    {
        return c;
    }

    public boolean intersect(final Triangle triB)
    {
        boolean rv;

        rv = lineTriIntersection(this.a, this.b, triB);
        if (rv)
            return true;

        rv = lineTriIntersection(this.b, this.c, triB);
        if (rv)
            return true;

        //actually that this one returns true should never happen because in my theory it should be atleast 2 lines "pierce" the triangle_b
        rv = lineTriIntersection(this.a, this.c, triB);

        return rv;
    }

    public boolean lineTriIntersection(final Vector3 p, final Vector3 q, final Triangle triangle)
    {
        final Vector3 pInverted = Vector3.invert(p);
        final Vector3 pq = Vector3.add(q, pInverted);
        final Vector3 pa = Vector3.add(triangle.a, pInverted);
        final Vector3 pb = Vector3.add(triangle.b, pInverted);
        final Vector3 pc = Vector3.add(triangle.c, pInverted);

        final float u = Vector3.scalarTriple(pq, pc, pb);
        if (u < 0) return false;

        final float v = Vector3.scalarTriple(pq, pa, pc);
        if (v < 0) return false;

        final float w = Vector3.scalarTriple(pq, pb, pa);
        if (w < 0) return false;

        return true;
    }

}