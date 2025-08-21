package io.github.luigeneric.core.sector.management.lootsystem;


import io.github.luigeneric.core.User;
import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.List;

public record PlayerLootItems(User user, List<ItemCountable> itemCountables)
{
}
