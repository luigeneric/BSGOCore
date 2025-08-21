package io.github.luigeneric.core.spaceentities;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class DynamicEventTrigger extends SpaceObject
{
    private final long sectorEventCardGuid;

    public DynamicEventTrigger(
            long objectID, OwnerCard ownerCard, WorldCard worldCard,
            SpaceEntityType spaceEntityType, Faction faction, FactionGroup factionGroup, SpaceSubscribeInfo spaceSubscribeInfo,
            final long sectorEventCardGuid
    )
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, spaceSubscribeInfo);
        this.sectorEventCardGuid = sectorEventCardGuid;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeGUID(sectorEventCardGuid);
    }
}
