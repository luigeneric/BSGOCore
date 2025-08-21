package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.core.movement.DynamicMovementController;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.templates.cards.MovementCard;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class Comet extends SpaceObject
{
    private final MovementCard movementCard;
    public Comet(final long objectID, OwnerCard ownerCard, WorldCard worldCard,
                 final MovementCard movementCard, final SpaceSubscribeInfo spaceSubscribeInfo)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Comet, Faction.Neutral, FactionGroup.Group0, spaceSubscribeInfo);
        this.movementCard = movementCard;
    }

    @Override
    public void createMovementController(final Transform transform)
    {
        this.movementController = new DynamicMovementController(transform, this.movementCard);
    }
}
