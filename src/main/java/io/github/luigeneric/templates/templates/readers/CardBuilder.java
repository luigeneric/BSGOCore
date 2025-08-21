package io.github.luigeneric.templates.templates.readers;

import com.google.gson.*;
import io.github.luigeneric.templates.cards.*;
import io.github.luigeneric.templates.deserializer.ShipItemDeserializer;
import io.github.luigeneric.templates.shipitems.ShipItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CardBuilder extends TemplateReader
{

    public CardBuilder(Path path)
    {
        super(path);
    }
    @Inject
    public CardBuilder()
    {
        this(Paths.get("ServerConfigurationUtils","global", "JsonCards"));
    }


    public Card[] fetchAllCards2() throws IllegalStateException
    {
        final List<Path> fileNames = getFilePaths();
        final Gson gson = gsonBuilder.create();
        List<JsonArray> jsonArrays = new ArrayList<>();

        for (final Path path : fileNames)
        {
            final String allText = readRawTxt(path);
            if (allText == null) continue;
            //cardsArray.add(gsonBuilder.create().fromJson(allText, Card[].class));
            //final Card[] cards = gson.fromJson(allText, Card[].class);
            //cardsArray.put(path.toString(), cards);
            try
            {
                jsonArrays.add(gson.fromJson(allText, JsonArray.class));
            }
            catch (JsonSyntaxException jsonSyntaxException)
            {
                jsonSyntaxException.printStackTrace();
                throw jsonSyntaxException;
            }
        }

        final JsonArray merged = new JsonArray();
        for (JsonArray jsonArray : jsonArrays)
        {
            merged.addAll(jsonArray);
        }

        return gson.fromJson(merged, Card[].class);
    }

    @Override
    protected void registerAllDeserializer()
    {
        final JsonDeserializer<Card> cardJsonDeserializer = (json, typeOfT, context) ->
        {
            final JsonObject jsonObject = json.getAsJsonObject();
            final int intValue = jsonObject.get("cardView").getAsInt();
            final CardView cardView = CardView.valueOf(intValue);
            Card card = null;
            switch (cardView)
            {
                case GUI -> card = context.deserialize(json, GuiCard.class);
                case ShipSystem -> card = context.deserialize(json, ShipSystemCard.class);
                case ShipConsumable -> card = context.deserialize(json, ShipConsumableCard.class);
                case World -> card = context.deserialize(json, WorldCard.class);
                case Global -> card = context.deserialize(json, GlobalCard.class);
                case ShipAbility -> card = context.deserialize(json, ShipAbilityCard.class);
                case Counter -> card = context.deserialize(json, CounterCard.class);
                case Skill -> card = context.deserialize(json, SkillCard.class);
                case Ship -> card = context.deserialize(json, ShipCard.class);
                case Sector -> card = context.deserialize(json, SectorCard.class);
                case Starter -> card = context.deserialize(json, StarterCard.class);
                case Room -> card = context.deserialize(json, RoomCard.class);
                case Mission -> card = context.deserialize(json, MissionCard.class);
                case Reward -> card = context.deserialize(json, RewardCard.class);
                case Title -> card = context.deserialize(json, TitleCard.class);
                case Duty -> card = context.deserialize(json, DutyCard.class);
                case AvatarCatalogue -> card = context.deserialize(json, AvatarCatalogueCard.class);
                case Module -> card = context.deserialize(json, ModuleCard.class);
                case Price -> card = context.deserialize(json, ShopItemCard.class);
                case Missile -> card = context.deserialize(json, MissileCard.class);
                case ShipList -> card = context.deserialize(json, ShipListCard.class);
                case StickerList -> card = context.deserialize(json, StickerListCard.class);
                case Movement -> card = context.deserialize(json, MovementCard.class);
                case Owner -> card = context.deserialize(json, OwnerCard.class);
                case GalaxyMap -> card = context.deserialize(json, GalaxyMapCard.class);
                case Camera -> card = context.deserialize(json, CameraCard.class);
                case MailTemplate -> card = context.deserialize(json, MailTemplateCard.class);
                case StarterKit -> card = context.deserialize(json, StarterKitCard.class);
                case ShipPaint -> card = context.deserialize(json, ShipSystemPaintCard.class);
                case Regulation -> card = context.deserialize(json, RegulationCard.class);
                case ShipSale -> card = context.deserialize(json, ShipSaleCard.class);
                case SectorEvent -> card = context.deserialize(json, SectorEventCard.class);
                case Tournament -> card = context.deserialize(json, TournamentCard.class);
                case ShipLight -> card = context.deserialize(json, ShipCardLight.class);
                case EventShop -> card = context.deserialize(json, EventShopCard.class);
                case GlobalBonusEvent -> card = context.deserialize(json, GlobalBonusEventCard.class);
                case Banner -> card = context.deserialize(json, BannerCard.class);
                case ConversionCampaign -> card = context.deserialize(json, SpecialOfferCard.class);
                //TODO ZoneCard is broken in both ends (parser + here) FIX
                case Zone -> card = context.deserialize(json, ZoneCard.class);
                case NonShipStats -> card = context.deserialize(json, NonShipStatsCard.class);
            }

            return card;
        };

        final var jsonCardArrayDeserializer = new JsonDeserializer<Card[]>()
        {
            @Override
            public Card[] deserialize(final JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                final JsonArray jsonArray = json.getAsJsonArray();
                System.out.println("size: " + jsonArray.size());
                final List<Card> cards = new ArrayList<>();
                for (final JsonElement jsonElement : jsonArray)
                {
                    cards.add(context.deserialize(jsonElement, Card.class));
                }

                return cards.toArray(new Card[0]);
            }
        };

        gsonBuilder.registerTypeAdapter(Card[].class, jsonCardArrayDeserializer);
        gsonBuilder.registerTypeAdapter(ShipItem.class, new ShipItemDeserializer());
        gsonBuilder.registerTypeAdapter(Card.class, cardJsonDeserializer);

    }
}
