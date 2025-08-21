package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

/**
 * @implSpec This is ALWAYS a WaypointSphere-PrefabName in the client, so this will be always the same!
 */
public class BsgoTrigger extends SpaceObject
{
    private final String name;
    private final Vector3 position;
    private final float radius;

    public BsgoTrigger(long objectID, OwnerCard ownerCard, WorldCard worldCard, SpaceEntityType spaceEntityType,
                       final Faction faction, FactionGroup factionGroup, SpaceSubscribeInfo spaceSubscribeInfo,
                       final String name, final Vector3 position, final float radius)
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, spaceSubscribeInfo);
        this.name = name;
        this.position = position;
        this.radius = radius;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(this.name);
        bw.writeVector3(this.position);
        bw.writeSingle(this.radius);
    }
}
