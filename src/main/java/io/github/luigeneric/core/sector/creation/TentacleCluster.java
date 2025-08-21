package io.github.luigeneric.core.sector.creation;


import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.*;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.AsteroidTemplate;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.enterprise.inject.spi.CDI;

import java.util.ArrayList;
import java.util.List;

public class TentacleCluster implements SpaceGroupCreatable
{
    private final Sector sector;
    private final BgoRandom rnd;
    private final long rndFromCenter;
    private final long asteroidCount;
    private final long tentacleCount;
    private final float angleOffset;

    private Transform parentTransform;

    private final List<SpaceObject> tentacleAsteroids;
    private final Catalogue catalogue;

    public TentacleCluster(Sector sector, BgoRandom rnd,
                        long rndFromCenter, long asteroidCount, long tentacleCount, float angleOffset)
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.sector = sector;
        this.rnd = rnd;
        this.rndFromCenter = rndFromCenter;
        this.asteroidCount = asteroidCount;
        this.tentacleCount = tentacleCount;
        this.angleOffset = angleOffset;
        this.tentacleAsteroids= new ArrayList<>();
    }


    @Override
    public SpaceObject getParent() throws IllegalAccessException
    {
        throw new IllegalAccessException("Cluster has no parent");
    }

    @Override
    public List<SpaceObject> getChildren()
    {
        return this.tentacleAsteroids;
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
        for (int j = 0; j < tentacleCount; j++)
        {
            Euler3 rotation = new Euler3(0,rnd.getRndBetween(-180,180) ,rnd.getRndBetween(-90,90));
            for (int i = 0; i <= asteroidCount; i++)
            {

                float distance = Mathf.pow(i, 2);
                float yTentacleOffset = rnd.getRndBetween(-angleOffset,angleOffset);
                float zTentacleOffset = rnd.getRndBetween(-angleOffset,angleOffset);

                rotation = Euler3.fromQuaternion(rotation.quaternion().mult(new Euler3(0, yTentacleOffset,
                        zTentacleOffset).quaternion()));


                final Vector3 pos = rotation.quaternion().mult(StaticVectors.RIGHT).mult(distance).add(positionOffset);

                final WorldCard rndAsteroidWorldCard = rnd.getRandomItemOfList(allAsteroidsWorldCards);
                final AsteroidTemplate tmpAsteroidTemplate =
                        new AsteroidTemplate(
                                rndAsteroidWorldCard.getCardGuid(),
                                SpaceEntityType.Asteroid, CreatingCause.AlreadyExists,
                                1, true, pos,
                                Euler3.zero(), rnd.getRndBetween(10f, 50f), 0
                        );
                final Asteroid tmpAsteroid = sector.getCtx().spaceObjectFactory().createAsteroid(tmpAsteroidTemplate, 20f);
                this.tentacleAsteroids.add(tmpAsteroid);
            }
        }
    }
    private void createParentTransform(final Vector3 positionOffset)
    {
        this.parentTransform = new Transform(positionOffset, Quaternion.identity(), true);
    }
}