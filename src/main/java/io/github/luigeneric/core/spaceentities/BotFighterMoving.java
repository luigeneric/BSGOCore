package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.core.movement.DynamicMovementController;
import io.github.luigeneric.core.sector.npcbehaviour.KillObjective;
import io.github.luigeneric.core.sector.npcbehaviour.NpcObjective;
import io.github.luigeneric.core.sector.npcbehaviour.NpcObjectiveType;
import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.bindings.ShipBindings;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplate;

import java.util.List;

public class BotFighterMoving extends NpcShip
{
    public BotFighterMoving(final long objectID, final OwnerCard ownerCard, final WorldCard worldCard, final ShipCard shipCard,
                            Faction faction, FactionGroup factionGroup,
                            ShipBindings shipBindings, ShipAspects shipAspects,
                            SpaceSubscribeInfo shipSubscribeInfo,
                            final NpcBehaviourTemplate npcBehaviourTemplate,
                            final List<NpcObjective> npcObjectives,
                            final long timeCreated)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.BotFighter, faction, factionGroup, shipBindings,
                shipAspects, shipSubscribeInfo, shipCard, npcBehaviourTemplate, npcObjectives, timeCreated);
    }

    @Override
    public void createMovementController(final Transform transform)
    {
        this.movementController = new DynamicMovementController(transform, movementCard);
    }

    public boolean hasObjectives()
    {
        return this.npcObjectives != null && !this.npcObjectives.isEmpty();
    }
    @Override
    public boolean hasKillObjectives()
    {
        if (this.npcObjectives == null)
            return false;
        final List<KillObjective> killObjectives = this.npcObjectives.stream()
                .filter(obj -> obj.getType() == NpcObjectiveType.Kill)
                .map(obj -> (KillObjective)obj)
                .toList();
        if (killObjectives.isEmpty())
            return false;
        return !killObjectives.get(0).getObjectivesToKill().isEmpty();
    }
}
