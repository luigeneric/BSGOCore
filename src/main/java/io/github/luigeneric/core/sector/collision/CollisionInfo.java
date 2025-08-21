package io.github.luigeneric.core.sector.collision;


import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.linearalgebra.collidershapes.CollisionRecord;

public record CollisionInfo(CollisionRecord collisionRecord, SpaceObject object1, SpaceObject object2)
{

    @Override
    public String toString()
    {
        return "CollisionInfo{" +
                "contactPointInfo=" + collisionRecord +
                ", object1=" + object1 +
                ", object2=" + object2 +
                '}';
    }
}

