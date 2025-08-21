package io.github.luigeneric.core.sector;

import io.github.luigeneric.core.sector.collision.CollisionPair;
import io.github.luigeneric.core.sector.management.SpaceObjectRemover;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;

import java.util.ArrayList;
import java.util.List;

public class SectorUtils
{
    public static void removeAllAsteroids(final Sector sector)
    {
        for (SpaceObject value : sector.ctx.spaceObjects().values())
        {
            if (value.getSpaceEntityType() != SpaceEntityType.Asteroid)
                continue;

            //addSpaceObjectRemoveRequest(value.getObjectID(), RemovingCause.Death);
            sector.getSpaceObjectRemover().notifyRemovingCauseAdded(value, RemovingCause.Death);
        }
    }

    public static void removeAllNonPlayerObjects(final Sector sector)
    {
        final List<SpaceObject> allNonPlayerObjects = sector.ctx.spaceObjects().getSpaceObjectsNotOfEntityType(SpaceEntityType.Player);
        for (final SpaceObject value : allNonPlayerObjects)
        {
            sector.getSpaceObjectRemover().notifyRemovingCauseAdded(value, RemovingCause.Death);
        }
    }

    public static void removeCollidingAsteroids2(final Sector sector)
    {
        final SpaceObjectRemover remover = sector.getSpaceObjectRemover();
        final List<SpaceObject> allAsteroids = sector.ctx.spaceObjects()
                .getSpaceObjectsOfEntityType(SpaceEntityType.Asteroid);
        final List<SpaceObject> others = sector.ctx.spaceObjects()
                .getSpaceObjectsOfEntityTypes(SpaceEntityType.Planetoid, SpaceEntityType.Outpost);

        final List<CollisionPair> collisionPairsAsteroidXAsteroid = new ArrayList<>();
        final List<CollisionPair> collisionPairsAsteroidXOthers = new ArrayList<>();

        //get pairs for asteroids x asteroids
        for (int i = 0; i < allAsteroids.size(); i++)
        {
            final SpaceObject first = allAsteroids.get(i);
            for (int j = i; j < allAsteroids.size(); j++)
            {
                final SpaceObject second = allAsteroids.get(j);
                if (first == second)
                    continue;

                final boolean collides = first.getCollider().collidesPrimitive(second.getCollider());
                if (collides)
                    collisionPairsAsteroidXAsteroid.add(new CollisionPair(first, second));
            }
            for (final SpaceObject other : others)
            {
                final boolean collides = first.getCollider().collidesPrimitive(other.getCollider());
                if (collides)
                    collisionPairsAsteroidXOthers.add(new CollisionPair(first, other));
            }
        }

        //remove the asteroid in asteroid x others
        for (final CollisionPair collisionPairsAsteroidXOther : collisionPairsAsteroidXOthers)
        {
            remover.notifyRemovingCauseAdded(collisionPairsAsteroidXOther.first(), RemovingCause.Death);
        }
        //asteroid x asteroid, remove first if both present
        for (final CollisionPair collisionPair : collisionPairsAsteroidXAsteroid)
        {
            var first = collisionPair.first();
            var second = collisionPair.second();

            if (first.isRemoved() || second.isRemoved())
                continue;

            remover.notifyRemovingCauseAdded(first, RemovingCause.Death);
        }
    }
}
