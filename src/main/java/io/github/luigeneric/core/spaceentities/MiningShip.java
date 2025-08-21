package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.bindings.ShipBindings;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class MiningShip extends Ship
{
    private final Planetoid attachedToPlanetoid;
    private final User owner;
    private long lastTimeMining;
    private long lastTimeAssassin;


    public MiningShip(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, Faction faction,
                      final SpaceSubscribeInfo spaceSubscribeInfo, final User owner, final Planetoid attachedToPlanetoid,
                      final ShipCard shipCard)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.MiningShip, faction, FactionGroup.Group0, new ShipBindings(),
                new ShipAspects(), spaceSubscribeInfo, shipCard);
        this.owner = owner;
        this.attachedToPlanetoid = attachedToPlanetoid;
        this.lastTimeMining = 0;
        this.lastTimeAssassin = 0;
    }

    @Override
    public boolean spawnedBy(final SpaceObject spawner)
    {
        return this.owner.getPlayer().getUserID() == spawner.getPlayerId();
    }

    public Planetoid getAttachedToPlanetoid()
    {
        return attachedToPlanetoid;
    }

    public User getOwner()
    {
        return owner;
    }

    public long getLastTimeMining()
    {
        return lastTimeMining;
    }

    public void setLastTimeMining(long lastTimeMining)
    {
        this.lastTimeMining = lastTimeMining;
    }

    public long getLastTimeAssassin()
    {
        return lastTimeAssassin;
    }

    public void setLastTimeAssassin(final long lastTimeAssassin)
    {
        this.lastTimeAssassin = lastTimeAssassin;
    }
}
