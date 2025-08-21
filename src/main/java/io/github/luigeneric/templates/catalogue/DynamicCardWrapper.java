package io.github.luigeneric.templates.catalogue;

import io.github.luigeneric.templates.cards.Card;
import io.github.luigeneric.templates.cards.CardView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamicCardWrapper
{
    private final Set<CardView> initialRequestedViews;
    private final Map<CardView, Card> cardMap;
    private boolean allCardsAvailable;

    public DynamicCardWrapper(final CardView... cardViews)
    {
        this(Set.of(cardViews));
    }
    DynamicCardWrapper(final Set<CardView> initialRequestedViews)
    {
        this.initialRequestedViews = initialRequestedViews;
        this.cardMap = new HashMap<>();
        this.allCardsAvailable = false;
    }

    public void put(final Card card)
    {
        this.cardMap.put(card.getCardView(), card);
    }

    private boolean isAllCardsAvailable()
    {
        if (allCardsAvailable)
            return true;
        final boolean initialMatches = checkInitialViewsMatchCurrent();
        if (initialMatches)
            allCardsAvailable = true;
        return initialMatches;
    }
    private boolean checkInitialViewsMatchCurrent()
    {
        return cardMap.keySet().equals(initialRequestedViews);
    }

    @SuppressWarnings("unchecked")
    public <T extends Card> T getCard(CardView view)
    {
        if (!isAllCardsAvailable())
            throw new IllegalArgumentException("Requested view is not available");
        return (T) cardMap.get(view);
    }
}
