package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.ICopy;

public record Capsule(Vector3 a, Vector3 b, float radius) implements ICopy<Capsule>
{

    @Override
    public Capsule copy()
    {
        return new Capsule(this.a.copy(), this.b.copy(), this.radius);
    }
}
