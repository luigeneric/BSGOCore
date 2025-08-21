package io.github.luigeneric.core.sector.management.lootsystem;


import io.github.luigeneric.core.User;
import io.github.luigeneric.enums.SpecialAction;

import java.util.ArrayList;
import java.util.List;

public record PvpResult(User user, List<ItemCountableLootEntryInfo> rolledItems, long experience, List<SpecialAction> specialActions)
{
    public PvpResult(User user, List<ItemCountableLootEntryInfo> rolledItems, long experience, SpecialAction specialAction)
    {
        this(user, rolledItems, experience, new ArrayList<>(List.of(specialAction)));
    }

    public boolean containsAction(final SpecialAction... actionsToCheck)
    {
        for (final SpecialAction action : this.specialActions)
        {
            for (final SpecialAction other : actionsToCheck)
            {
                if (action == other)
                    return true;
            }
        }
        return false;
    }
}
