package io.github.luigeneric.templates.catalogue;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.*;
import io.github.luigeneric.templates.templates.readers.CardBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Singleton
public class Catalogue
{
    private final CardBuilder cardBuilder;
    private final Map<Long, Card> cardMap;
    private final List<SkillCard> skillCards;
    private final Map<Long, MissionCard> missionCardsMap;
    private final Map<Long, BgoProtocolWriter> cardWriters;
    private StaticCards staticCards;


    public Catalogue(final CardBuilder cardBuilder)
    {
        this.cardBuilder = cardBuilder;
        this.cardMap = new HashMap<>();
        this.missionCardsMap = new HashMap<>();
        this.cardWriters = new HashMap<>();
        this.skillCards = new ArrayList<>();
        setupCards();
    }

    public GalaxyMapCard galaxyMapCard()
    {
        return staticCards.galaxyMapCard();
    }
    public GlobalCard globalCard()
    {
        return staticCards.globalCard();
    }

    public final void setupCards() throws IllegalStateException
    {
        final Card[] cards = cardBuilder.fetchAllCards2();
        addCards(cards);

        skillCards.addAll(getAllCardsOfView(CardView.Skill));

        List<MissionCard> missionCards = getAllCardsOfView(CardView.Mission);
        for (MissionCard missionCard : missionCards)
        {
            missionCardsMap.put(missionCard.getCardGuid(), missionCard);
        }

        final List<Long> freeCardGuids = getFreeCardGUIDs(100);
        log.info("next free CARD-GUIDS: {}", freeCardGuids);

        staticCards = new StaticCards(fetchCardUnsafe(StaticCardGUID.GlobalCard, CardView.Global),
                fetchCardUnsafe(StaticCardGUID.GalaxyMap, CardView.GalaxyMap));
    }

    public BgoProtocolWriter getProtocolWriter(final long cardGUID, final CardView view)
    {
        return cardWriters.get(generateKey(cardGUID, view));
    }
    public void putProtocolWriter(final long cardGUID, final CardView view, final BgoProtocolWriter bw)
    {
        cardWriters.put(generateKey(cardGUID, view), bw);
    }

    public MissionCardsFetchResult fetchMissionCards(final long missionGuid)
    {
        final Optional<MissionCard> optMissionCard = fetchCard(missionGuid, CardView.Mission);
        if (optMissionCard.isEmpty())
        {
            log.warn("Fetch error in catalogue for mission {}", missionGuid);
            return MissionCardsFetchResult.invalid();
        }

        final MissionCard missionCard = optMissionCard.get();
        final Optional<RewardCard> optionalRewardCard = fetchCard(missionCard.getRewardCardGuid(), CardView.Reward);
        if (optionalRewardCard.isEmpty())
        {
            log.warn("Fetch error in catalogue for mission reward {}", missionCard.getRewardCardGuid());
            return MissionCardsFetchResult.invalid();
        }
        final RewardCard rewardCard = optionalRewardCard.get();

        return new MissionCardsFetchResult(missionCard, rewardCard);
    }
    public SystemPriceCards fetchSystemPriceCards(final long cardGuid)
    {
        final Optional<ShipSystemCard> optionalShipSystemCard = fetchCard(cardGuid, CardView.ShipSystem);
        if (optionalShipSystemCard.isEmpty())
        {
            log.warn("Fetch error in catalogue for systemguid {}", cardGuid);
            return SystemPriceCards.invalid();
        }

        final ShipSystemCard shipSystemCard = optionalShipSystemCard.get();
        final Optional<ShopItemCard> optionalShopItemCard = fetchCard(cardGuid, CardView.Price);
        if (optionalShopItemCard.isEmpty())
        {
            log.warn("Fetch error in catalogue for shipsystempricefetch price {}", cardGuid);
            return SystemPriceCards.invalid();
        }
        final ShopItemCard shopItemCard = optionalShopItemCard.get();

        return new SystemPriceCards(shipSystemCard, shopItemCard);
    }
    public Map<Byte, ShipSystemCard> fetchAllSystemCards(final long guid) throws IllegalArgumentException
    {
        final Map<Byte, ShipSystemCard> shortShipSystemCardMap = new HashMap<>();

        final Optional<ShipSystemCard> optShipSystemCard = fetchCard(guid, CardView.ShipSystem);
        if (optShipSystemCard.isEmpty())
            throw new IllegalArgumentException("guid was invalid");
        final ShipSystemCard initShipSystemCard = optShipSystemCard.get();
        shortShipSystemCardMap.put(initShipSystemCard.getLevel(), initShipSystemCard);
        ShipSystemCard current = initShipSystemCard;
        while (current.getNextCardGuid() != 0)
        {
            final Optional<ShipSystemCard> optNextCard = fetchCard(current.getNextCardGuid(), CardView.ShipSystem);
            if (optNextCard.isEmpty())
                break;
            final ShipSystemCard nextCard = optNextCard.get();
            current = nextCard;
            shortShipSystemCardMap.put(nextCard.getLevel(), nextCard);
        }

        return shortShipSystemCardMap;
    }

    public List<WorldCard> getAllWorldCardsOfPrefabStartWith(final String prefabStart)
    {
        return cardMap.values().stream()
                .filter(card -> card.getCardView() == CardView.World)
                .map(card -> (WorldCard)card)
                .filter(card -> card.getPrefabName().startsWith(prefabStart))
                .toList();
    }
    public Map<Long, MissionCard> getAllMissionCards()
    {
        return Collections.unmodifiableMap(missionCardsMap);
    }
    public List<WorldCard> getAllAsteroidWorldCards()
    {
        return cardMap.values().stream()
                .filter(card -> card.getCardView() == CardView.World)
                .map(card -> (WorldCard)card)
                .filter(card -> card.getPrefabName().contains("asteroid"))
                .filter(card -> !card.getPrefabName().contains("field"))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Card> List<T> getAllCardsOfView(final CardView cardView)
    {
        return (List<T>) cardMap.values().stream()
                .filter(card -> card.getCardView() == cardView)
                .toList();
    }

    public List<SkillCard> getAllSkilLCardsOfLevel(final short level)
    {
        final List<SkillCard> skillCardsOfLevel = new ArrayList<>();
        for (final SkillCard skillCard : skillCards)
        {
            if (skillCard.getLevel() == level)
            {
                skillCardsOfLevel.add(skillCard);
            }
        }

        return skillCardsOfLevel;
    }

    private void addCards(Card[] cards)
    {
        if (cards != null)
        {
            for(Card card : cards)
            {
                if (card != null)
                {
                    addCard(card);
                }
            }
        }
    }

    public Optional<SectorCard> getSectorCardByID(final long id)
    {
        final Optional<Card> guiCardPresent = cardMap.values().stream()
                .filter(card -> card.getCardView() == CardView.GUI)
                .filter(card -> ((GuiCard)card).getKey().equals("sector" + id))
                .findFirst();

        if (guiCardPresent.isEmpty())
            return Optional.empty();


        GuiCard guiCard = (GuiCard) guiCardPresent.get();
        return fetchCard(guiCard.getCardGuid(), CardView.Sector);
    }

    public DynamicCardWrapper fetchCards(final long guid, final CardView... cardViews)
    {
        final DynamicCardWrapper dynamicCardWrapper = new DynamicCardWrapper(cardViews);

        for (CardView cardView : cardViews) {
            final Optional<Card> optCard = fetchCard(guid, cardView);
            if (optCard.isEmpty()) {
                break;
            }
            Card card = optCard.get();
            dynamicCardWrapper.put(card);
        }
        return dynamicCardWrapper;
    }


    public WorldOwnerCard fetchWorldOwnerCards(final long guid) throws IllegalArgumentException
    {
        final Optional<WorldCard> optWorldCard = fetchCard(guid, CardView.World);
        final Optional<OwnerCard> optOwnerCard = fetchCard(guid, CardView.Owner);
        if (optOwnerCard.isEmpty())
            throw new IllegalArgumentException("Could not find OwnerCard for guid " + guid);
        if (optWorldCard.isEmpty())
            throw new IllegalArgumentException("Could not find WorldCard for guid " + guid);

        return new WorldOwnerCard(optWorldCard.get(), optOwnerCard.get());
    }

    public void addCard(final Card card)
    {
        final long key = generateKey(card.getCardGuid(), card.getCardView());
        final Card rv = cardMap.put(key, card);
        if (rv != null)
        {
            log.error("DOUBLE CARD; current: " + card + " before: " + rv);
        }
    }
    private static long generateKey(final long cardGUID, final CardView cardView)
    {
        long num = cardView.getValue();
        num <<= 32;
        return num + cardGUID;
        //return new Tuple<Long, CardView>(cardGUID, cardView);
    }

    public <T extends Card> T fetchCardUnsafe(final StaticCardGUID staticCardGUID, final CardView cardView)
    {
        return fetchCardUnsafe(staticCardGUID.getValue(), cardView);
    }
    @SuppressWarnings("unchecked")
    public <T extends Card> T fetchCardUnsafe(final long cardGUID, final CardView cardView)
    {
        final long key = generateKey(cardGUID, cardView);
        return (T) cardMap.get(key);
    }

    public <T extends Card> Optional<T> fetchCard(final StaticCardGUID staticCardGUID, final CardView cardView)
    {
        return fetchCard(staticCardGUID.getValue(), cardView);
    }

    @SuppressWarnings("unchecked")
    public <T extends Card> Optional<T> fetchCard(final long cardGuid, final CardView cardView)
    {
        if(cardGuid == 0)
        {
            return Optional.empty();
        }
        //Log.infoIn("Received a card request! CardGuid: " + cardGuid + " |  CardView: " + cardView + "(" + cardView.getValue() + ")");

        final long key = generateKey(cardGuid, cardView);
        return Optional.ofNullable((T)cardMap.get(key));
    }

    public List<Long> getFreeCardGUIDs(final int count)
    {
        final List<Long> freeCardGuids = new ArrayList<>(count);

        for (long l = 1; l < Integer.MAX_VALUE * 2L; l++)
        {
            boolean exists = false;
            for (var cardView : CardView.values())
            {
                final long generatedKey = generateKey(l, cardView);
                if (cardMap.containsKey(generatedKey))
                {
                    exists = true;
                    break;
                }
            }
            if (!exists)
            {
                freeCardGuids.add(l);
                if (freeCardGuids.size() >= count)
                    break;
            }
        }

        return freeCardGuids;
    }


}
