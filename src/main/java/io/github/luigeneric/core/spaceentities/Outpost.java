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
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplate;

import java.util.List;

public class Outpost extends NpcShip
{
    public Outpost(long objectID, final OwnerCard ownerCard, final WorldCard worldCard,
                   final ShipCard shipCard, Faction faction, FactionGroup factionGroup,
                   ShipBindings shipBindings, ShipAspects shipAspects, SpaceSubscribeInfo shipSubscribeInfo,
                   final NpcBehaviourTemplate npcBehaviourTemplate, final long creatingTimeStamp)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Outpost, faction, factionGroup, shipBindings, shipAspects, shipSubscribeInfo, shipCard,
                npcBehaviourTemplate, List.of(), creatingTimeStamp);
    }
    public Outpost(long objectID, final OwnerCard ownerCard, final WorldCard worldCard,
                   final ShipCard shipCard, Faction faction, SpaceSubscribeInfo shipSubscribeInfo,
                   final NpcBehaviourTemplate npcBehaviourTemplate, final long creatingTimeStamp, final ShipAspects shipAspects)
    {
        this(objectID, ownerCard, worldCard, shipCard, faction, FactionGroup.Group0, new ShipBindings(), shipAspects, shipSubscribeInfo,
                npcBehaviourTemplate, creatingTimeStamp);
    }

    @Override
    public boolean hasKillObjectives()
    {
        return false;
    }
}

