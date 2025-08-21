package io.github.luigeneric.core.sector.creation;


import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.AsteroidTemplate;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.PlanetoidTemplate;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.enterprise.inject.spi.CDI;

import java.util.ArrayList;
import java.util.List;

public class PlanetoidWithAsteroids implements SpaceGroupCreatable
{
    private final SectorContext ctx;
    private final BgoRandom rnd;
    private final long countAsteroidsPerPlanetoid;
    private final float[] minDistInit;
    private SpaceObject parent;
    private final List<SpaceObject> children;
    private final Catalogue catalogue;

    public PlanetoidWithAsteroids(final SectorContext ctx,
                                  final BgoRandom rnd,
                                  final long countAsteroidsPerPlanetoid,
                                  final float[] minDistInit
    )
    {
        this.ctx = ctx;
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.rnd = rnd;
        this.countAsteroidsPerPlanetoid = countAsteroidsPerPlanetoid;
        if (minDistInit.length < 2) throw new IllegalArgumentException();
        this.minDistInit = minDistInit;
        this.children = new ArrayList<>();
    }

    @Override
    public SpaceObject getParent()
    {
        return this.parent;
    }

    @Override
    public List<SpaceObject> getChildren()
    {
        return this.children;
    }

    private void createPlanetoid()
    {
        final SpaceObjectFactory factory = ctx.spaceObjectFactory();
        final List<WorldCard> allPlanetoidWorldCards = catalogue.getAllWorldCardsOfPrefabStartWith("planetoid");
        final float height = ctx.blueprint().sectorCards().sectorCard().getHeight() * 0.5f;
        final float length = ctx.blueprint().sectorCards().sectorCard().getLength() * 0.5f;
        final float width = ctx.blueprint().sectorCards().sectorCard().getWidth() * 0.5f;

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

        this.parent = rndPlanetoid;
    }

    @Override
    public void create()
    {
        createPlanetoid();
        createChildren();
    }

    private void createChildren()
    {
        final SpaceObjectFactory factory = ctx.spaceObjectFactory();
        final List<WorldCard> allAsteroidsWorldCards = catalogue.getAllAsteroidWorldCards();


        final float minDist = rnd.getRndBetween(minDistInit[0], minDistInit[1]);
        final float maxDist = rnd.getRndBetween(minDist, minDist+rnd.getRndBetweenInt(300, 700));

        final MovementController planetoidMovementController = parent.getMovementController();
        final Transform planetoidTransform = planetoidMovementController.getTransform();

        for (long i = 0; i < countAsteroidsPerPlanetoid; i++)
        {


            final Vector3 pos = new Vector3(
                    rnd.getRndBetween(minDist, maxDist),
                    rnd.getRndBetweenInt(-300, 300),
                    0);

            final Euler3 rotation = new Euler3(0, 360f / countAsteroidsPerPlanetoid * i, 0);
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
            this.children.add(tmpAsteroid);
        }
    }
}
