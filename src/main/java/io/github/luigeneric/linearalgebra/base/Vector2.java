package io.github.luigeneric.linearalgebra.base;


import io.github.luigeneric.linearalgebra.utility.Mathf;

import java.util.Objects;

public class Vector2
{
    float x;
    float y;

    public Vector2(final float x, final float y)
    {
        this.x = x;
        this.y = y;
    }
    public Vector2(final Vector2 copy)
    {
        this(copy.x, copy.y);
    }

    public Vector2 copy()
    {
        return new Vector2(this);
    }

    public static Vector2 zero()
    {
        return new Vector2(0f, 0f);
    }


    public float magnitude()
    {
        return Mathf.sqrt(x * x + y * y);
    }

    public static float distance(final Vector2 a, final Vector2 b)
    {
        return sub(a, b).magnitude();
    }

    public static Vector2 sub(Vector2 a, Vector2 b)
    {
        return new Vector2(a.x - b.x, a.y - b.y);
    }


    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2 vector2 = (Vector2) o;
        return Float.compare(vector2.x, x) == 0 && Float.compare(vector2.y, y) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }

    @Override
    public String toString()
    {
        return "Vector2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
