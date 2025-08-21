package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class Asteroid extends SpaceObject
{
    protected final float radius;
    protected final float rotationSpeed;


    public Asteroid(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, SpaceEntityType spaceEntityType, Faction faction,
                    FactionGroup factionGroup, SpaceSubscribeInfo shipSubscribeInfo, final float radius, float rotationSpeed)
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, shipSubscribeInfo);
        this.radius = radius;
        this.rotationSpeed = rotationSpeed;
    }

    public Asteroid(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, SpaceSubscribeInfo shipSubscribeInfo,
                    final float radius, final float rotationSpeed)
    {
        this(objectID, ownerCard, worldCard, SpaceEntityType.Asteroid, Faction.Neutral, FactionGroup.Group0, shipSubscribeInfo,
                radius, rotationSpeed);
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeVector3(this.movementController.getPosition()); //3 * 4
        bw.writeSingle(this.radius); //4
        bw.writeSingle(this.rotationSpeed); //4
    }

    public float getRadius()
    {
        return radius;
    }

    public float getRotationSpeed()
    {
        return rotationSpeed;
    }
}
