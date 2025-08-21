package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.cards.ShipListCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ShipCardConverter
{
    private final ShipListCard colonialShipListCard;
    private final ShipListCard cylonShipListCard;
    private final Faction faction;
    private final Catalogue catalogue;

    public ShipCardConverter(final Faction faction)
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.colonialShipListCard = catalogue.fetchCardUnsafe(StaticCardGUID.ShipListCardColonial, CardView.ShipList);
        this.cylonShipListCard = catalogue.fetchCardUnsafe(StaticCardGUID.ShipListCardCylon, CardView.ShipList);
        this.faction = faction;
    }

    public List<ShipCard> convertAllCards(final List<ShipCard> currentCards) throws IllegalStateException
    {
        List<ShipCard> newCards = new ArrayList<>(currentCards.size());

        for (ShipCard currentCard : currentCards)
        {
            try
            {
                newCards.add(convert(currentCard));
            }
            catch (IllegalStateException illegalStateException)
            {
                log.info(illegalStateException.getMessage());
            }

        }
        if (newCards.size() != currentCards.size())
        {
            //throw new IllegalStateException("New Cards size is not equal to current card size! new " + newCards.size() + " current " + currentCards.size());
        }

        return newCards;
    }

    private ShipCard convert(final ShipCard shipCard) throws IllegalStateException
    {
        final byte hangarId = shipCard.getHangarId();
        final boolean isUpgrade = shipCard.getNextShipCardGuid() == 0;
        final long[] idsToSearchIn = isUpgrade ? getOpposite().getUpgradeShipCardGuids() : getOpposite().getShipCardGuids();

        final Optional<ShipCard> optShip = findInGuids(idsToSearchIn, hangarId);
        if (optShip.isEmpty())
            throw new IllegalStateException("Cannot find opposite shipCard!");
        return optShip.get();
    }

    private ShipListCard getCurrent()
    {
        return this.faction == Faction.Colonial ? colonialShipListCard : cylonShipListCard;
    }
    private ShipListCard getOpposite()
    {
        return this.faction == Faction.Cylon ? colonialShipListCard : cylonShipListCard;
    }

    private Optional<ShipCard> findInGuids(final long[] guids, final byte hangarId)
    {
        return Arrays.stream(guids)
                .mapToObj(this::getForGuid)
                .filter(shipCard -> shipCard.getHangarId() == hangarId)
                .findAny();
    }
    private ShipCard getForGuid(final long guid)
    {
        return catalogue.fetchCardUnsafe(guid, CardView.Ship);
    }
}
