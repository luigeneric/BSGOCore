package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.utils.Color;

public class Planet extends SpaceObject
{
    private final Vector3 position;
    private final Quaternion rotation;
    private final float scale;
    private final Color color;
    private final Color specularColor;
    private final float shininess;
    public Planet(long objectID, OwnerCard ownerCard, WorldCard worldCard, Vector3 position, Quaternion rotation,
                  float scale, Color color, Color specularColor, float shininess)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Planet, Faction.Neutral,
                FactionGroup.Group0, new SpaceSubscribeInfo(objectID, new ObjectStats()));

        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.color = color;
        this.specularColor = specularColor;
        this.shininess = shininess;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeVector3(this.position);
        bw.writeQuaternion(this.rotation);
        bw.writeSingle(this.scale);
        bw.writeColor(this.color);
        bw.writeColor(this.specularColor);
        bw.writeSingle(this.shininess);
    }

    public float getScale()
    {
        return this.scale;
    }

    public Vector3 getPosition()
    {
        return position;
    }

    public Quaternion getRotation()
    {
        return rotation;
    }

    public Color getColor()
    {
        return color;
    }

    public Color getSpecularColor()
    {
        return specularColor;
    }

    public float getShininess()
    {
        return shininess;
    }
}
