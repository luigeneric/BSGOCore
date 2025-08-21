package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class DebrisPile extends SpaceObject
{
    private final Vector3 scale;
    private final float rotationSpeed;

    public DebrisPile(long objectID, OwnerCard ownerCard, WorldCard worldCard,
                      FactionGroup factionGroup, SpaceSubscribeInfo spaceSubscribeInfo,
                      Vector3 scale, float rotationSpeed)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Debris, Faction.Neutral, factionGroup, spaceSubscribeInfo);
        this.scale = scale;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeVector3(this.movementController.getPosition());
        bw.writeQuaternion(this.movementController.getRotation());
        bw.writeVector3(scale);
        bw.writeSingle(this.rotationSpeed);
    }
}
