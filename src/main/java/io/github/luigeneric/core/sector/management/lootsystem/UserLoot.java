package io.github.luigeneric.core.sector.management.lootsystem;


import io.github.luigeneric.core.User;
import io.github.luigeneric.enums.SpecialAction;

import java.util.List;

public record UserLoot(User user, long experience, List<ItemCountableBonusType> items, List<SpecialAction> specialAction)
{
}

