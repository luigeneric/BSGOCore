package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.bindings.ShipBindings;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class CruiserShip extends Ship
{
    public CruiserShip(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, Faction faction,
                       FactionGroup factionGroup, ShipBindings shipBindings,
                       ShipAspects shipAspects, SpaceSubscribeInfo shipSubscribeInfo, final ShipCard shipCard)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Cruiser,
                faction, factionGroup, shipBindings, shipAspects, shipSubscribeInfo, shipCard);
    }
}
