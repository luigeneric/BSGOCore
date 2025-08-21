package io.github.luigeneric.core.protocols.debug;

import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.cards.ShopItemCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.catalogue.SystemPriceCards;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class RefundProcessor
{
    private final Catalogue catalogue;

    public RefundProcessor(final Catalogue catalogue)
    {
        this.catalogue = catalogue;
    }




    public Map<Byte, Float> getSummedPriceForLevels(final long lvl1Guid)
    {
        final Map<Byte, SystemPriceCards> systemPriceMap = getAllCardsForRefundItem(lvl1Guid);
        final Map<Byte, Float> priceMapForEachLevel = getAllCardPrices(systemPriceMap);
        final Map<Byte, Float> priceMapForEachLevelSummedToTheLevel = getAllCardPricesSummedUp(priceMapForEachLevel);
        return priceMapForEachLevelSummedToTheLevel;
    }

    private Map<Byte, Float> getAllCardPricesSummedUp(final Map<Byte, Float> priceMapForEachLevel)
    {
        final Map<Byte, Float> resultMap = new LinkedHashMap<>();
        float summedValue = 0;
        for (final Map.Entry<Byte, Float> entry : priceMapForEachLevel.entrySet())
        {
            summedValue += entry.getValue();
            resultMap.put(entry.getKey(), summedValue);
        }
        return resultMap;
    }

    private Map<Byte, SystemPriceCards> getAllCardsForRefundItem(final long initialGuid)
    {
        final SystemPriceCards first = catalogue.fetchSystemPriceCards(initialGuid);
        final Map<Byte, SystemPriceCards> systemPriceCardsMap = new LinkedHashMap<>();
        systemPriceCardsMap.put(first.shipSystemCard().getLevel(), first);

        SystemPriceCards current = first;
        while (current.isValid() && current.shipSystemCard().getNextCardGuid() != 0)
        {
            final SystemPriceCards next = catalogue.fetchSystemPriceCards(current.shipSystemCard().getNextCardGuid());
            systemPriceCardsMap.put(next.shipSystemCard().getLevel(), next);

            current = next;
        }

        return systemPriceCardsMap;
    }

    private Map<Byte, Float> getAllCardPrices(final Map<Byte, SystemPriceCards> systemPriceMap)
    {
        final Map<Byte, Float> resultMap = new LinkedHashMap<>();
        for (final SystemPriceCards systemPriceCards : systemPriceMap.values())
        {
            if (systemPriceCards.shipSystemCard().getNextCardGuid() == 0)
                continue;

            final ShopItemCard shopItemCard = systemPriceCards.shopItemCard();
            final float cubitsPrice = shopItemCard.getUpgradePrice().getFor(ResourceType.Cubits.guid);
            //log.debug("CubitsPrice for {} is {}", systemPriceCards.shipSystemCard().getLevel(), cubitsPrice);
            resultMap.put((byte) (systemPriceCards.shipSystemCard().getLevel()+1), cubitsPrice);
        }

        return resultMap;
    }
}

