package io.github.luigeneric.core.sector.management.lootsystem;


import io.github.luigeneric.core.User;
import io.github.luigeneric.enums.SpecialAction;
import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.Collections;
import java.util.List;

public record UserItems(User user, List<ItemCountable> itemCountables, long exp, List<SpecialAction> specialActions)
{
    public UserItems(User user, List<ItemCountable> itemCountables, long exp)
    {
        this(user, itemCountables, exp, Collections.singletonList(SpecialAction.None));
    }

    public SpecialAction highestSpecialAction()
    {
        SpecialAction highestSpecialAction = SpecialAction.None;
        float highest = 0;
        for (int i = 0; i < specialActions.size(); i++)
        {
            final SpecialAction current = specialActions.get(i);
            if (current.lootMultiplier > highest)
            {
                highest = current.lootMultiplier;
                highestSpecialAction = current;
            }
        }
        return highestSpecialAction;
    }
}
