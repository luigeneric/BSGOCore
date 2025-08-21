package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.core.sector.npcbehaviour.KillObjective;
import io.github.luigeneric.core.sector.npcbehaviour.NpcObjective;
import io.github.luigeneric.core.sector.npcbehaviour.NpcObjectiveType;
import io.github.luigeneric.core.sector.npcbehaviour.PatrolObjective;
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

public abstract class NpcShip extends Ship
{
    protected final NpcBehaviourTemplate npcBehaviourTemplate;
    protected final List<NpcObjective> npcObjectives;
    protected final long creatingTimeStamp;
    public NpcShip(final long objectID, final OwnerCard ownerCard,
                   final WorldCard worldCard, final SpaceEntityType spaceEntityType,
                   final Faction faction, final FactionGroup factionGroup,
                   final ShipBindings shipBindings, final ShipAspects shipAspects,
                   final SpaceSubscribeInfo shipSubscribeInfo, final ShipCard shipCard,
                   final NpcBehaviourTemplate npcBehaviourTemplate, List<NpcObjective> npcObjectives, long creatingTimeStamp)
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, shipBindings, shipAspects, shipSubscribeInfo, shipCard);
        this.npcBehaviourTemplate = npcBehaviourTemplate;
        this.npcObjectives = npcObjectives;
        this.creatingTimeStamp = creatingTimeStamp;
    }

    public NpcBehaviourTemplate getNpcBehaviourTemplate()
    {
        return npcBehaviourTemplate;
    }

    public abstract boolean hasKillObjectives();
    public List<NpcObjective> getNpcObjectives()
    {
        return npcObjectives;
    }
    public List<KillObjective> getKillObjectives()
    {
        return this.npcObjectives.stream()
                .filter(obj -> obj.getType() == NpcObjectiveType.Kill)
                .map(obj -> (KillObjective)obj)
                .toList();
    }
    public List<PatrolObjective> getPatrolObjectives()
    {
        return this.npcObjectives.stream()
                .filter(obj -> obj.getType() == NpcObjectiveType.Patrol)
                .map(obj -> (PatrolObjective)obj)
                .toList();
    }

    public long getCreatingTimeStamp()
    {
        return creatingTimeStamp;
    }
}
