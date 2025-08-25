package io.github.luigeneric.core.protocols.wof;

import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.Hold;
import io.github.luigeneric.core.player.container.visitors.ShopVisitor;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.debug.DebugProtocol;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.ItemPicker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class WofProtocol extends BgoProtocol
{
    private final List<Integer> costListPerStep;
    private final ItemPicker<ShipItem> shipItemItemPicker;
    private ShipItem jackpotItem;
    private final WofProtocolWriteOnly writer;
    private BgoTimeStamp lastWofGameSession;
    public WofProtocol(final ProtocolContext ctx)
    {
        super(ProtocolID.Wof, ctx);
        this.writer = new WofProtocolWriteOnly();

        this.costListPerStep = new ArrayList<>();
        this.setupCostList();
        this.shipItemItemPicker = new ItemPicker<>(ctx.rng());

        setupWofItems();
    }

    private List<ShipItem> getJackpotPool()
    {
        List<ShipItem> jackPotItems = new ArrayList<>();

        jackPotItems.add(ItemCountable.fromGUID(ResourceType.Cubits, 10_000));
        jackPotItems.add(ItemCountable.fromGUID(ResourceType.TuningKit, 10));
        jackPotItems.add(ItemCountable.fromGUID(ResourceType.Strikex5Nuke, 1));
        jackPotItems.add(ItemCountable.fromGUID(ResourceType.Escortx5Nuke, 1));
        jackPotItems.add(ItemCountable.fromGUID(ResourceType.Linerx5Nuke, 1));

        return jackPotItems;
    }

    private void setupCostList()
    {
        for (int i = 1; i <= 6; i++)
        {
            this.costListPerStep.add(200 * i + (i-1)*60);
        }
    }

    private void setupJackpot()
    {
        //jackpot
        final List<ShipItem> jackpotItems = getJackpotPool();
        final int idx = LocalDateTime.now(Clock.systemUTC()).getDayOfYear() % jackpotItems.size();
        this.jackpotItem = jackpotItems.get(idx);
    }

    /// TODO refactor into config utils for maintainability
    /// This is work in progress
    private void setupWofItems()
    {
        setupJackpot();

        this.shipItemItemPicker.add(jackpotItem, 1);
        //"normal" items
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 100), 70);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 200), 60);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 400), 50);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 800), 40);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 1600), 30);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 3200), 20);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Tylium, 6400), 10);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 100), 70);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 200), 60);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 400), 50);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 800), 40);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 1600), 30);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 3200), 20);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.Titanium, 6400), 10);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.StrikerStandard_Rounds, 250), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.StrikerStandard_Missiles, 25), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.StrikerGreen_Rounds, 125), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.LinerGreen_Missiles, 12), 5);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.EscortStandard_Rounds, 125), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.EscortStandard_Missiles, 12), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.EscortGreen_Rounds, 75), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.EscortGreen_Missiles, 6), 5);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.LinerStandard_Rounds, 62), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.LinerStandard_Missiles, 6), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.LinerGreen_Rounds, 31), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.LinerGreen_Missiles, 3), 5);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.TechnicalAnalysisKit, 1), 30);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.TechnicalAnalysisKit, 3), 15);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.TuningKit, 1), 2);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.CommAccess, 1), 5);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.CommAccess, 3), 5);

        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.YellowBox, 1), 3);
        this.shipItemItemPicker.add(ItemCountable.fromGUID(ResourceType.GreenBox, 1), 2);
    }

    public static Optional<BgoTimeStamp> getLastFreeWofPlayed(final User user)
    {
        return user.getPlayer().getLastFreeWofGame();
    }


    public void sendInit()
    {
        final long amount = switch (jackpotItem)
        {
            case ItemCountable itemCountable -> itemCountable.getCount();
            case null, default -> 1;
        };

        final BgoProtocolWriter bw = writer
                .writeInit(
                        JackpotType.Item,
                        this.jackpotItem.getCardGuid(),
                        amount,
                        isFreeWofGame(),
                        costListPerStep
                );

        user().send(bw);
    }

    private boolean isFreeWofGame()
    {
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        LocalDate date = now.toLocalDate();
        final var lastFreeWofGameTs = getLastFreeWofPlayed(user());

        return lastFreeWofGameTs
                .map(bgoTimeStamp -> bgoTimeStamp.getLocalDate().isBefore(date.atStartOfDay()))
                .orElse(true);
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientRequest clientRequest = ClientRequest.forValue((short) msgType);
        switch (clientRequest)
        {
            case RequestInit ->
            {
                setupJackpot();
                sendInit();
                user().send(writer.writeAllVisibleMaps());
            }
            case RequestDraw ->
            {
                final int drawCount = br.readInt32();
                final DebugProtocol debugProtocol = user().getProtocol(ProtocolID.Debug);
                final short minLevel = 5;
                if (user().getPlayer().getSkillBook().get() < minLevel)
                {
                    debugProtocol.sendEzMsg("You have to be level " + minLevel);
                    return;
                }
                final BgoTimeStamp lastGame = lastWofGameSession == null ?
                        new BgoTimeStamp(LocalDateTime.now().minusDays(1)) : lastWofGameSession;
                BgoTimeStamp now = BgoTimeStamp.now();
                final long durationMillis = lastGame.totalDurationMsBetween(now);
                if (durationMillis < 2000)
                {
                    log.warn("Cheat fromUser[{}], Dradiscontact wofspam duration: {}", user().getUserLog(), durationMillis);
                }
                ShopVisitor shopVisitor = new ShopVisitor(user(), null, ctx.rng());
                this.lastWofGameSession = now;


                if (drawCount < 1 || drawCount > 6)
                {
                    log.warn(user().getUserLog() + "Draw Request of not allowed drawCount -> Client modification ");
                    return;
                }

                //check for enough cubits in hangar
                final Hold hold = user().getPlayer().getHold();

                final boolean isFreeWofGame = isFreeWofGame();
                final int numReduction = isFreeWofGame ? 2 : 1;
                if (isFreeWofGame)
                {
                    user().getPlayer().setLastFreeWofGame(LocalDateTime.now(Clock.systemUTC()));
                }
                final Optional<ItemCountable> optCubits = hold.hasItemCountable(ResourceType.Cubits.guid);
                if (optCubits.isEmpty() && !isFreeWofGame)
                {
                    log.warn(user().getUserLog() + "not enough cubits in Hold, cheat detected");
                    return;
                }

                int costs;
                if (isFreeWofGame && drawCount == 1)
                {
                    costs = 0;
                }
                else
                {
                    costs = this.costListPerStep.get(drawCount - numReduction);
                }

                if (costs > 0)
                {
                    final boolean reduceSuccessfully = shopVisitor.reduceItemCountableByCount(ResourceType.Cubits, costs);
                    if (!reduceSuccessfully)
                    {
                        log.warn("Dradis cheater {}", user().getUserLog());
                        return;
                    }
                }

                final List<ShipItem> shipItemsToAdd = new ArrayList<>(drawCount);
                //fill items to add
                for (int i = 0; i < drawCount; i++)
                {
                    shipItemsToAdd.add(this.shipItemItemPicker.getRandomItem());
                }
                user().getPlayer().getCounterFacade().incrementCounter(CounterCardType.wof_played, 0, drawCount);
                // fraction
                /*
                PrometheusMetrics.INSTANCE.getWofDrawTotal()
                        .labels(this.user().getPlayer().getFaction().name())
                        .inc(drawCount);
                 */

                List<ShipItem> shipItemsToAddCleaned = new ArrayList<>(drawCount);
                for (ShipItem shipItem : shipItemsToAdd)
                {
                    ShipItem newItem;
                    if (shipItem instanceof ItemCountable itemCountable)
                    {
                        newItem = ItemCountable.fromGUID(itemCountable.getCardGuid(), itemCountable.getCount());
                    }
                    else
                    {
                        newItem = ShipSystem.fromGUID(shipItem.getCardGuid());
                    }
                    shopVisitor.addShipItem(newItem, hold);
                    shipItemsToAddCleaned.add(newItem);
                }

                user().send(writer.writeWofDrawReply(JackpotType.Item, shipItemsToAddCleaned, jackpotItem,
                        false));
                log.info("User {} wofdraw cnt: {}", user().getUserLogSimple(), drawCount);
                ctx.micrometerRegistry().wofPlayed(user().getPlayer().getFaction(), drawCount);
            }
            default -> log.info("WofProtocol: {} not implemented!", clientRequest);
        }
    }


    private enum ClientRequest
    {
        RequestInit(1),
        RequestDraw(3),
        RequestVisibleMaps(5),
        RequestMapStart(7),
        RequestMapInfo(8);

        public static final int SIZE = Short.SIZE;

        public final short shortValue;

        private static final class MappingsHolder
        {
            private static final Map<Short, ClientRequest> mappings = new HashMap<>();
        }

        private static Map<Short, ClientRequest> getMappings()
        {
            return MappingsHolder.mappings;
        }
        ClientRequest(final int value)
        {
            this((short) value);
        }

        ClientRequest(final short value)
        {
            shortValue = value;
            getMappings().put(value, this);
        }

        public short getValue()
        {
            return shortValue;
        }

        public static ClientRequest forValue(final short value)
        {
            return getMappings().get(value);
        }
    }


}
