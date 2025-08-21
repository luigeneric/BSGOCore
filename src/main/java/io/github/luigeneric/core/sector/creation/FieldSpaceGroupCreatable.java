package io.github.luigeneric.core.sector.creation;

import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.BgoRandom;

import java.util.ArrayList;
import java.util.List;

public class FieldSpaceGroupCreatable implements SpaceGroupCreatable
{
    private SpaceObject parent;
    private Transform parentTransform;
    private final List<SpaceObject> children;

    private final int countAsteroidsPerField;
    private final float sizeField;
    private final BgoRandom rnd;
    private final Sector sector;

    public FieldSpaceGroupCreatable(final int countAsteroidsPerField, final float sizeField, BgoRandom rnd, Sector sector)
    {
        this.countAsteroidsPerField = countAsteroidsPerField;
        this.sizeField = sizeField;
        this.rnd = rnd;
        this.sector = sector;
        this.children = new ArrayList<>();
    }

    @Override
    public SpaceObject getParent()
    {
        return null;
    }

    @Override
    public List<SpaceObject> getChildren()
    {
        return null;
    }

    @Override
    public void create()
    {
        createParent();
        createChildren();
    }

    private void createChildren()
    {

    }

    private void createParent()
    {
        var secCard = sector.getCtx().blueprint().sectorCards().sectorCard();
        final float height = secCard.getHeight() * 0.5f;
        final float length = secCard.getLength() * 0.5f;
        final float width = secCard.getWidth() * 0.5f;


        final Vector3 centerOfField = new Vector3(
                rnd.getRndBetween(-width, width),
                rnd.getRndBetween(-height/2, height/2),
                rnd.getRndBetween(-length, length));
        this.parentTransform = new Transform(centerOfField, Quaternion.identity());
    }
}
