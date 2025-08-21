package io.github.luigeneric.linearalgebra.intersection;

import io.github.luigeneric.linearalgebra.collidershapes.CollisionRecord;
import io.github.luigeneric.linearalgebra.collidershapes.OBBCollider;

public interface IOOBBIntersection
{
    CollisionRecord checkForCollision(final OBBCollider a, final OBBCollider b);
}
