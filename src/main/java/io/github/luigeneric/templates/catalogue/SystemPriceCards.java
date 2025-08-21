package io.github.luigeneric.templates.catalogue;


import io.github.luigeneric.templates.cards.ShipSystemCard;
import io.github.luigeneric.templates.cards.ShopItemCard;

public record SystemPriceCards(ShipSystemCard shipSystemCard, ShopItemCard shopItemCard)
{
    public static SystemPriceCards invalid()
    {
        return new SystemPriceCards(null, null);
    }

    public boolean isValid()
    {
        return this.shipSystemCard != null && this.shopItemCard != null;
    }
}
