package io.github.luigeneric.core.sector.creation;

import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.AsteroidTemplate;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.PlanetoidTemplate;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class SectorRandomGenerationUtils
{
    private final Catalogue catalogue;

    /**
     * Last test
     * randomSektor 42 300 7 5 300 5000 3 1000 100//num, asteroid per plan, plan count, field count, asteroids per field, field size
     * @param sector the sector to use this for
     * @param rnd BgoRandom object with helper methods
     * @param countAsteroids num asteroids around planetoid
     * @param countPlanetoids num planetoids at random positions
     * @param countFields field count
     * @param countAsteroidsPerField asteroids in each field at random position
     * @param sizeField scales the field
     * @param loopCount loop = cylinder
     * @param loopSize cylinder size
     * @param loopAsteroidCount num asteroids at cylinder surface
     */
    public void buildRandomSectorSpaceObjects(final Sector sector, final BgoRandom rnd,
                                              final long countAsteroids, final long countPlanetoids,
                                              final long countFields, final long countAsteroidsPerField, final long sizeField,
                                              final long loopCount, final long loopSize, final long loopAsteroidCount)
    {
        final SpaceObjectFactory factory = sector.getCtx().spaceObjectFactory();
        final List<WorldCard> allPlanetoidWorldCards = catalogue.getAllWorldCardsOfPrefabStartWith("planetoid");
        final List<WorldCard> allAsteroidsWorldCards = catalogue.getAllAsteroidWorldCards()
                .stream()
                .filter(worldCard -> !worldCard.getPrefabName().contains("field"))
                .toList();

        var sectorCards = sector.getCtx().blueprint().sectorCards();
        final float height = sectorCards.sectorCard().getHeight() * 0.5f;
        final float length = sectorCards.sectorCard().getLength() * 0.5f;
        final float width =  sectorCards.sectorCard().getWidth() * 0.5f;

        final List<Planetoid> randomPlanetoids = new ArrayList<>();
        final List<Asteroid> randomAsteroids = new ArrayList<>();

        final List<SpaceObject> finalSpaceObjects = new ArrayList<>();

        for (long i = 0; i < countPlanetoids; i++)
        {
            final Vector3 rndPosition = new Vector3(
                    rnd.getRndBetween(-width, width),
                    rnd.getRndBetween(-height/2, height/2),
                    rnd.getRndBetween(-length, length));
            final WorldCard rndPlanetoidWorldCard = rnd.getRandomItemOfList(allPlanetoidWorldCards);
            final PlanetoidTemplate tmpTemplate = new PlanetoidTemplate(rndPlanetoidWorldCard.getCardGuid(),

                    SpaceEntityType.Planetoid, CreatingCause.AlreadyExists, 1, true, rndPosition,
                    Euler3.zero(), 1, 0);
            final Planetoid rndPlanetoid = factory.createPlanetoid(tmpTemplate);
            rndPlanetoid.createMovementController(tmpTemplate.getTransform());

            //sector.getSectorJoinQueue().addSpaceObject(rndPlanetoid);
            randomPlanetoids.add(rndPlanetoid);
        }


        for (final Planetoid randomPlanetoid : randomPlanetoids)
        {
            final float minDist = rnd.getRndBetweenInt(1100, 1300);
            final float maxDist = rnd.getRndBetween(minDist, minDist+rnd.getRndBetweenInt(300, 700));

            for (long i = 0; i < countAsteroids; i++)
            {
                final MovementController planetoidMovementController = randomPlanetoid.getMovementController();
                final Transform planetoidTransform = planetoidMovementController.getTransform();

                final Vector3 pos = new Vector3(
                        rnd.getRndBetween(minDist, maxDist),
                        rnd.getRndBetweenInt(-300, 300),
                        0);

                final Euler3 rotation = new Euler3(0, 360f / countAsteroids * i, 0);
                final Vector3 newPos = rotation.quaternion().mult(pos);

                final Vector3 insideGlobal = planetoidTransform.applyTransform(newPos);


                final WorldCard rndAsteroidWorldCard = rnd.getRandomItemOfList(allAsteroidsWorldCards);
                final AsteroidTemplate tmpAsteroidTemplate =
                        new AsteroidTemplate(
                                rndAsteroidWorldCard.getCardGuid(),
                                SpaceEntityType.Asteroid, CreatingCause.AlreadyExists,
                                1, true, insideGlobal,
                                Euler3.zero(), rnd.getRndBetween(10f, 50f), 0
                        );
                final Asteroid tmpAsteroid = factory.createAsteroid(tmpAsteroidTemplate, 20f);
                randomAsteroids.add(tmpAsteroid);
            }
        }

        for (long indexField = 0; indexField < countFields; indexField++)
        {
            //find pos of field
            final Vector3 centerOfField = new Vector3(
                    rnd.getRndBetween(-width, width),
                    rnd.getRndBetween(-height/2, height/2),
                    rnd.getRndBetween(-length, length));
            final Transform fieldTransform = new Transform(centerOfField, new Euler3(0, 0, 0).quaternion());


            float doX = rnd.getRndBetweenInt(0, 4);
            float doY = rnd.getRndBetweenInt(0, 4);
            float doZ = rnd.getRndBetweenInt(0, 4);
            if (doX == 0 && doY == 0 && doZ == 0)
                doX = 1;

            for (long indexAsteroidInField = 0; indexAsteroidInField < countAsteroidsPerField; indexAsteroidInField++)
            {
                final Vector3 localPos = Vector3.zero();
                localPos.addX((float) sizeField/(float) countAsteroids * indexAsteroidInField);


                Euler3 localRot = new Euler3(
                        doX * 360f/countAsteroidsPerField*indexAsteroidInField,
                        doY * 360f/countAsteroidsPerField*indexAsteroidInField,
                        doZ * 360f/countAsteroidsPerField*indexAsteroidInField);
                localPos.set(localRot.quaternion().mult(localPos));


                final Vector3 globalPos = fieldTransform.applyTransform(localPos);

                final WorldCard rndAsteroidWorldCard = rnd.getRandomItemOfList(allAsteroidsWorldCards);
                AsteroidTemplate tmpAsteroidTemplate = new AsteroidTemplate(rndAsteroidWorldCard.getCardGuid(),
                        SpaceEntityType.Asteroid, CreatingCause.AlreadyExists, 1, true, globalPos,
                        Euler3.zero(), rnd.getRndBetween(10f, 70f), 0);
                final Asteroid tmpAsteroid = factory.createAsteroid(tmpAsteroidTemplate, 20f);
                randomAsteroids.add(tmpAsteroid);
            }
        }


        for (long loopCountIndex = 0; loopCountIndex < loopCount; loopCountIndex++)
        {
            final Vector3 rndPosition = new Vector3(
                    rnd.getRndBetween(-width, width),
                    rnd.getRndBetween(-height/2, height/2),
                    rnd.getRndBetween(-length, length));
            final Transform transform = new Transform(rndPosition, Quaternion.identity());

            for (long loopAsteroidIndex = 0; loopAsteroidIndex < loopAsteroidCount; loopAsteroidIndex++)
            {
                Vector3 localPosition = new Vector3();
                localPosition.addX(rnd.getRndNegPosOf(loopSize));
                localPosition.addY(rnd.getRndBetween(-loopSize, loopSize));

                Euler3 localRotation = new Euler3(0,360f/loopAsteroidCount*loopAsteroidIndex, 0);


                localPosition = localRotation.quaternion().mult(localPosition);
                final Vector3 globalPos = transform.applyTransform(localPosition);
                final WorldCard rndAsteroidWorldCard = rnd.getRandomItemOfList(allAsteroidsWorldCards);
                AsteroidTemplate tmpAsteroidTemplate = new AsteroidTemplate(rndAsteroidWorldCard.getCardGuid(),
                        SpaceEntityType.Asteroid, CreatingCause.AlreadyExists, 1, true, globalPos,
                        Euler3.zero(), rnd.getRndBetween(10f, 70f), 0);
                final Asteroid tmpAsteroid = factory.createAsteroid(tmpAsteroidTemplate, 20f);
                randomAsteroids.add(tmpAsteroid);
            }
        }

        finalSpaceObjects.addAll(randomPlanetoids);
        finalSpaceObjects.addAll(randomAsteroids);
        for (final SpaceObject finalSpaceObject : finalSpaceObjects)
        {
            sector.getSectorJoinQueue().addSpaceObject(finalSpaceObject);
        }
    }

    public void buildRandomCreatable(final Sector sector,
                                     final SpaceGroupCreatable spaceGroupCreatable)
    {
        spaceGroupCreatable.create();
        final List<SpaceObject> children = spaceGroupCreatable.getChildren();
        final SectorJoinQueue joinQueue = sector.getSectorJoinQueue();

        try
        {
            final SpaceObject parent = spaceGroupCreatable.getParent();
            joinQueue.addSpaceObject(parent);
        }
        catch (Exception ignored)
        {

        }
        for (SpaceObject child : children)
        {
            joinQueue.addSpaceObject(child);
        }
    }
    public void buildRandomRing(final Sector sector, final BgoRandom random,
                                long rndFromCenter, long asteroidCount, float minDistance, float maxDistance, float angleOffset)
    {
        final AsteroidRing asteroidRing = new AsteroidRing(sector, random, rndFromCenter,
                asteroidCount, minDistance, maxDistance, angleOffset);
        buildRandomCreatable(sector, asteroidRing);
    }
}
