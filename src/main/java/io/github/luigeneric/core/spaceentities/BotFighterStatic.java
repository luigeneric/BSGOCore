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

public class BotFighterStatic extends NpcShip
{
    public BotFighterStatic(long objectID,
                            final OwnerCard ownerCard,
                            final WorldCard worldCard,
                            final ShipCard shipCard, Faction faction,
                            ShipBindings shipBindings,
                            ShipAspects shipAspects,
                            SpaceSubscribeInfo spaceSubscribeInfo,
                            final NpcBehaviourTemplate npcBehaviourTemplate,
                            final long creatingTimeStamp
    )
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.BotFighter, faction, FactionGroup.Group0,
                shipBindings, shipAspects, spaceSubscribeInfo, shipCard, npcBehaviourTemplate, List.of(), creatingTimeStamp);
    }
    public BotFighterStatic(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, final ShipCard shipCard, Faction faction,
                          SpaceSubscribeInfo spaceSubscribeInfo, final NpcBehaviourTemplate npcBehaviourTemplate, final long creatingTimeStamp)
    {
        this(objectID, ownerCard, worldCard, shipCard, faction, new ShipBindings(), new ShipAspects(), spaceSubscribeInfo, npcBehaviourTemplate,
                creatingTimeStamp);
    }

    @Override
    public boolean hasKillObjectives()
    {
        return false;
    }
}
