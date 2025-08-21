package io.github.luigeneric.linearalgebra.collidershapes;


import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.ICopy;

public record Obb(Vector3 center, Vector3 halfWidthExtents) implements ICopy<Obb>
{

    @Override
    public Obb copy()
    {
        return new Obb(this.center.copy(), this.halfWidthExtents.copy());
    }
}
