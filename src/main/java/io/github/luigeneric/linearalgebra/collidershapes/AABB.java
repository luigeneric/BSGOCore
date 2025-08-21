package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Vector3;

import java.util.Objects;

public final class AABB
{
    private final Vector3 min;
    private final Vector3 max;

    public AABB(final Vector3 min, final Vector3 max)
    {
        this(min, max, true);
    }
    public AABB(final Vector3 min, final Vector3 max, final boolean copy)
    {
        if (copy)
        {
            this.min = min.copy();
            this.max = max.copy();
        }
        else
        {
            this.min = min;
            this.max = max;
        }
    }

    public Vector3 center()
    {
        return Vector3.add(min, max).mult(0.5f);
    }

    public boolean isVectorInBounds(final Vector3 v)
    {
        // Überprüfen, ob jeder Komponente des Vektors v zwischen den entsprechenden Komponenten von a und b liegt.
        final boolean xInRange = (v.getX() >= Math.min(min.getX(), max.getX())) && (v.getX() <= Math.max(min.getX(), max.getX()));
        if (!xInRange)
            return false;
        final boolean yInRange = (v.getY() >= Math.min(min.getY(), max.getY())) && (v.getY() <= Math.max(min.getY(), max.getY()));
        if (!yInRange)
            return false;
        final boolean zInRange = (v.getZ() >= Math.min(min.getZ(), max.getZ())) && (v.getZ() <= Math.max(min.getZ(), max.getZ()));

        return zInRange;
    }

    public Vector3 min()
    {
        return min;
    }

    public Vector3 max()
    {
        return max;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AABB) obj;
        return Objects.equals(this.min, that.min) &&
                Objects.equals(this.max, that.max);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(min, max);
    }

    @Override
    public String toString()
    {
        return "AABB{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
