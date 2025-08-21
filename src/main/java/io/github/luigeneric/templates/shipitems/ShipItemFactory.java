package io.github.luigeneric.templates.shipitems;


import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShopItemCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Optional;

public final class ShipItemFactory
{
    private static final Catalogue catalogue = CDI.current().select(Catalogue.class).get();
    private ShipItemFactory(){}

    public static ShipItem createFromGuid(final ItemType itemType, final long guid, final long count)
            throws IllegalArgumentException, IllegalStateException
    {
        switch (itemType)
        {
            case System ->
            {
                return ShipSystem.fromGUID(guid);
            }
            case Countable ->
            {
                return ItemCountable.fromGUID(guid, count);
            }
            case Ship ->
            {
                return new StoreShip(guid, 0);
            }
            default -> throw new IllegalStateException("ItemType " + itemType + " not implemented");
        }
    }

    public static ShipItem createFromGuid(final long guid) throws IllegalArgumentException
    {
        final Optional<ShopItemCard> optShopItemCard = catalogue.fetchCard(guid, CardView.Price);
        if (optShopItemCard.isEmpty())
            throw new IllegalArgumentException("Cannot find ShopItemCard for guid " + guid);
        final ShopItemCard shopItemCard = optShopItemCard.get();
        final ItemType type = shopItemCard.getShopCategory().getType();
        return createFromGuid(type, guid, 1);
    }

    public static ItemCountable createCountable(final long guid, final long count)
    {
        return ItemCountable.fromGUID(guid, count);
    }
}
