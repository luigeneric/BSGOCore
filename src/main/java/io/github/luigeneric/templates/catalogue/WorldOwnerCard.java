package io.github.luigeneric.templates.catalogue;


import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

import java.util.Objects;

public record WorldOwnerCard(WorldCard worldCard, OwnerCard ownerCard)
{
    public WorldOwnerCard
    {
        Objects.requireNonNull(worldCard);
        Objects.requireNonNull(ownerCard);
    }
}
