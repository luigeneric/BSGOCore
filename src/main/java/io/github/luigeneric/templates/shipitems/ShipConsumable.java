package io.github.luigeneric.templates.shipitems;


import io.github.luigeneric.templates.cards.ShipConsumableCard;

public class ShipConsumable
{
    private final ItemCountable itemCountable;
    private final ShipConsumableCard shipConsumableCard;


    public ShipConsumable(final ItemCountable itemCountable, final ShipConsumableCard shipConsumableCard)
    {
        this.itemCountable = itemCountable;
        this.shipConsumableCard = shipConsumableCard;
    }

    public ItemCountable getItemCountable()
    {
        return itemCountable;
    }

    public ShipConsumableCard getShipConsumableCard()
    {
        return shipConsumableCard;
    }
}
