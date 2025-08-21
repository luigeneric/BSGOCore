package io.github.luigeneric.core.spaceentities;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;
import lombok.Getter;

/**
 * Not used anymore
 */
@Getter
@Deprecated
public class CaptureTrigger extends SpaceObject
{
    private final long parent;
    private final float radius;

    public CaptureTrigger(long objectID, OwnerCard ownerCard, WorldCard worldCard, SpaceEntityType spaceEntityType, Faction faction, FactionGroup factionGroup, SpaceSubscribeInfo spaceSubscribeInfo, long parent, float radius)
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, spaceSubscribeInfo);
        this.parent = parent;
        this.radius = radius;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(parent);
        bw.writeSingle(radius);
    }
}


