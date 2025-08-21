package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.SectorEventCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class SectorEvent extends SpaceObject
{
    private final SectorEventCard sectorEventCard;
    public SectorEvent(long objectID, OwnerCard ownerCard, WorldCard worldCard, Faction faction, FactionGroup factionGroup,
                       SpaceSubscribeInfo spaceSubscribeInfo, SectorEventCard sectorEventCard)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.SectorEvent, faction, factionGroup, spaceSubscribeInfo);
        this.sectorEventCard = sectorEventCard;
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeGUID(this.sectorEventCard.getCardGuid());

    }
}
