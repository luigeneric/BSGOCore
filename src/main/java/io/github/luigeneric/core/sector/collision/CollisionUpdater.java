package io.github.luigeneric.core.sector.collision;

import io.github.luigeneric.core.sector.IntersectionFilter;
import io.github.luigeneric.core.sector.SectorJob;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.collidershapes.Collider;
import io.github.luigeneric.linearalgebra.collidershapes.CollisionRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CollisionUpdater implements SectorJob
{
    private final SectorSpaceObjects spaceObjects;
    private final CollisionResolution collisionResolution;
    private final IntersectionFilter intersectionFilter;
    private final Tick tick;

    public CollisionUpdater(final SectorSpaceObjects sectorSpaceObjects,
                            final CollisionResolution collisionResolution,
                            final IntersectionFilter intersectionFilter,
                            final Tick tick)
    {
        this.spaceObjects = sectorSpaceObjects;
        this.collisionResolution = collisionResolution;
        this.intersectionFilter = intersectionFilter;
        this.tick = tick;
    }


    private List<CollisionPair> preparePairsToCheck2()
    {
        final List<CollisionPair> collisionPairs = new ArrayList<>();
        final List<SpaceObject> objects = this.spaceObjects.getSpaceObjectsNotOfEntityType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Missile,
                SpaceEntityType.Comet
        );
        final int size = objects.size();

        for (int i = 0; i < size; i++)
        {
            final SpaceObject a = objects.get(i);
            if (a.getCollider() == null)
            {
                continue;
            }

            for (int j = i+1; j < size; j++)
            {
                final SpaceObject b = objects.get(j);
                if (a == b) //should never happen
                    continue;
                if (b.getCollider() == null)
                {
                    continue;
                }

                final boolean needsTest = intersectionFilter.needsIntersectionTest(a, b);
                if (needsTest)
                {
                    final CollisionPair pair = new CollisionPair(a, b);
                    collisionPairs.add(pair);
                }
            }
        }

        return collisionPairs;
    }



    private List<CollisionInfo> actualCollisionCheck(final List<CollisionPair> pairsToCheck)
    {
        final List<CollisionInfo> pairsCollided = new ArrayList<>();

        for (final CollisionPair collisionPair : pairsToCheck)
        {
            final Collider firstCollider = collisionPair.first().getCollider();
            final Collider secondCollider = collisionPair.second().getCollider();
            if (firstCollider == null || secondCollider == null)
                continue;

            final CollisionRecord collisionRecord = firstCollider.collides(secondCollider);

            if (collisionRecord != null)
            {
                pairsCollided.add(new CollisionInfo(collisionRecord, collisionPair.first(), collisionPair.second()));
            }

        }

        return pairsCollided;
    }

    @Override
    public void run()
    {
        //updates the positions
        this.newCollisionIteration();
        final List<CollisionPair> primitiveResults = this.getPrimitiveCollisionResults();
        final List<CollisionInfo> contactResults = this.getCollisionResults();

        this.collisionResolution.setCurrentTick(this.tick.copy());
        this.collisionResolution.injectPrimitiveAndContactInfos(primitiveResults, contactResults);
        //primitive collision resolution
        this.collisionResolution.resolveAll();
    }

    public void newCollisionIteration()
    {

        //ignore asteroids -> biggest speedup
        for (final SpaceObject spaceObject : this.spaceObjects.getSpaceObjectsNotOfEntityType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Planetoid,
                SpaceEntityType.Planet,
                SpaceEntityType.Debris
        ))
        {
            if (!spaceObject.getMovementController().isMovingObject())
                continue;
            final Collider collider = spaceObject.getCollider();
            if (collider != null)
            {
                collider.updatePositions();
            }
        }
    }

    private List<CollisionInfo> getCollisionResults()
    {
        final List<CollisionPair> pairsToCheck = preparePairsToCheck2();
        return this.actualCollisionCheck(pairsToCheck);
    }
    private List<CollisionPair> primitiveCheck()
    {
        final List<CollisionPair> pairs = new ArrayList<>();
        final List<SpaceObject> missiles = this.spaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Missile);

        //check all asteroids against all players and comets
        for (SpaceObject asteroid : this.spaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Asteroid))
        {
            for (SpaceObject playerComet : this.spaceObjects.getSpaceObjectsOfEntityTypes(SpaceEntityType.Player, SpaceEntityType.Comet))
            {
                final CollisionPair result = checkPrimitive(asteroid, playerComet);
                if (result != null)
                {
                    pairs.add(result);
                }
            }
        }

        //comets against each other
        final List<SpaceObject> comets = this.spaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Comet);
        for (SpaceObject cometOuter : comets)
        {
            for (SpaceObject cometInner : comets)
            {
                if (cometInner == cometOuter)
                    continue;

                final CollisionPair result = checkPrimitive(cometOuter, cometInner);
                if (result != null)
                {
                    pairs.add(result);
                }
            }
        }


        //all types of ship + planetoid + comet + debris
        final List<SpaceObject> testAgainstMissiles = this.spaceObjects.getSpaceObjectsOfTypeShip();
        testAgainstMissiles.addAll(this.spaceObjects.getSpaceObjectsOfEntityTypes(SpaceEntityType.Planetoid, SpaceEntityType.Comet, SpaceEntityType.Debris));

        for (final SpaceObject missile : missiles)
        {
            for (final SpaceObject spaceObject : testAgainstMissiles)
            {
                final boolean needsIntersection = intersectionFilter.testMissilePrimitive(missile, spaceObject);
                if (needsIntersection)
                {
                    final CollisionPair result = checkPrimitive(missile, spaceObject);
                    if (result != null)
                    {
                        pairs.add(result);
                    }
                }
            }
        }
        return pairs;
    }

    private CollisionPair checkPrimitive(final SpaceObject a, final SpaceObject b)
    {
        final Collider aCollider = a.getCollider();
        final Collider bCollider = b.getCollider();
        if (aCollider == null || bCollider == null)
        {
            //log.error("Exit, one collider was null " + a + " " + b);
            //return null;
            return null;
        }

        final boolean collides = aCollider.collidesPrimitive(bCollider);

        if (!collides)
        {
            return null;
        }

        return new CollisionPair(a, b);
    }

    public List<CollisionPair> getPrimitiveCollisionResults()
    {
        return primitiveCheck();
    }
}