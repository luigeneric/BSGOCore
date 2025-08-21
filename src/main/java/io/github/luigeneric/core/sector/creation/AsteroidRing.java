package io.github.luigeneric.core.sector.creation;


import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.*;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.AsteroidTemplate;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.enterprise.inject.spi.CDI;

import java.util.ArrayList;
import java.util.List;

public class AsteroidRing implements SpaceGroupCreatable
{
    private final Sector sector;
    private final BgoRandom rnd;
    private final long rndFromCenter;
    private final long asteroidCount;
    private final float minDistance;
    private final float maxDistance;
    private final float angleOffset;
    private final Catalogue catalogue;

    private Transform parentTransform;

    private final List<SpaceObject> ringAsteroids;

    public AsteroidRing(Sector sector, BgoRandom rnd,
                        long rndFromCenter, long asteroidCount, float minDistance, float maxDistance, float angleOffset)
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.sector = sector;
        this.rnd = rnd;
        this.rndFromCenter = rndFromCenter;
        this.asteroidCount = asteroidCount;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.angleOffset = angleOffset;
        this.ringAsteroids = new ArrayList<>();
    }


    @Override
    public SpaceObject getParent() throws IllegalAccessException
    {
        throw new IllegalAccessException("Ring has no parent");
    }

    @Override
    public List<SpaceObject> getChildren()
    {
        return this.ringAsteroids;
    }

    @Override
    public void create()
    {

        final List<WorldCard> allAsteroidsWorldCards = catalogue.getAllAsteroidWorldCards();
        final Vector3 positionOffset = new Vector3(
                rnd.getRndBetween(-rndFromCenter, rndFromCenter),
                rnd.getRndBetween(-rndFromCenter/10, rndFromCenter/10),
                rnd.getRndBetween(-rndFromCenter, rndFromCenter));
        createParentTransform(positionOffset);

        float randomAngle = rnd.nextFloat(0, 30f);
        float offset = rnd.nextFloat(0, 180f);
        for (int i = 0; i <= asteroidCount; i++)
        {

            float rndDist = rnd.getRndBetween(minDistance, maxDistance);
            float step = 360f / asteroidCount;
            float scaledAngleOffset= (rndDist/maxDistance)*angleOffset;
            float scale = i / (float)asteroidCount;
            Euler3 rotation = new Euler3(
                    0,
                    step* i + offset,
                    Math.sin(scale * Math.PI * 2) * randomAngle+rnd.getRndBetween(-scaledAngleOffset,scaledAngleOffset));
            //Vector3 pos = rotation * Vector3.right() * rndDist + positionOffset;
            final Vector3 pos = rotation.quaternion().mult(StaticVectors.RIGHT).mult(rndDist).add(positionOffset);

            final WorldCard rndAsteroidWorldCard = rnd.getRandomItemOfList(allAsteroidsWorldCards);
            final AsteroidTemplate tmpAsteroidTemplate =
                    new AsteroidTemplate(
                            rndAsteroidWorldCard.getCardGuid(),
                            SpaceEntityType.Asteroid, CreatingCause.AlreadyExists,
                            1, true, pos,
                            Euler3.zero(), rnd.getRndBetween(10f, 50f), 0
                    );
            final Asteroid tmpAsteroid = sector.getCtx().spaceObjectFactory().createAsteroid(tmpAsteroidTemplate, 20f);
            this.ringAsteroids.add(tmpAsteroid);
        }
    }
    private void createParentTransform(final Vector3 positionOffset)
    {
        this.parentTransform = new Transform(positionOffset, Quaternion.identity(), true);
    }
}
