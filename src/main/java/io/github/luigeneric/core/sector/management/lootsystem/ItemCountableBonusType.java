package io.github.luigeneric.core.sector.management.lootsystem;


import io.github.luigeneric.enums.LootBonusType;
import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.HashMap;
import java.util.Map;

public record ItemCountableBonusType(ItemCountable itemCountable, Map<LootBonusType, Long> lootBonusTypeLongMap)
{
    public ItemCountableBonusType(final ItemCountable itemCountable)
    {
        this(itemCountable, new HashMap<>());
    }
}
