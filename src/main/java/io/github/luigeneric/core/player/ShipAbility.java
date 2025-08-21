package io.github.luigeneric.core.player;

import io.github.luigeneric.templates.cards.ShipAbilityCard;
import io.github.luigeneric.templates.utils.ObjectStats;

import java.util.Objects;

public class ShipAbility
{
    private final ShipAbilityCard shipAbilityCard;
    private final ObjectStats itemBuffAdd;
    private final ObjectStats remoteBuffAdd;
    private final ObjectStats remoteBuffMultiply;

    public ShipAbility(final ShipAbilityCard shipAbilityCard)
    {
        Objects.requireNonNull(shipAbilityCard, "ShipAbilityCard is null");
        this.shipAbilityCard = shipAbilityCard;
        this.itemBuffAdd = new ObjectStats(this.shipAbilityCard.getItemBuffAdd().getCopy());
        this.remoteBuffAdd = new ObjectStats(this.shipAbilityCard.getRemoteBuffAdd().getCopy());
        this.remoteBuffMultiply = new ObjectStats(this.shipAbilityCard.getRemoteBuffMultiply().getCopy());
    }

    public void resetStats()
    {
        this.itemBuffAdd.put(this.shipAbilityCard.getItemBuffAdd());
    }

    public ShipAbilityCard getShipAbilityCard()
    {
        return shipAbilityCard;
    }

    public ObjectStats getItemBuffAdd()
    {
        return itemBuffAdd;
    }

    public ObjectStats getRemoteBuffMultiply()
    {
        return remoteBuffMultiply;
    }

    public ObjectStats getRemoteBuffAdd() {
        return remoteBuffAdd;
    }
}
