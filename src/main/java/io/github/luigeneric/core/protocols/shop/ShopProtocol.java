package io.github.luigeneric.core.protocols.shop;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.Hangar;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.enums.BgoAdminRoles;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.cards.Card;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipSystemCard;
import io.github.luigeneric.templates.cards.ShopItemCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.shipitems.StoreShip;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.templates.utils.Price;
import io.github.luigeneric.templates.utils.ShipSlotType;
import io.github.luigeneric.templates.utils.ShopCategory;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ShopProtocol extends BgoProtocol
{
    private final Set<Long> shipBlackList;
    private final Set<Long> consumableBlacklist;
    private final Set<Long> systemBlackList;
    private Shop shop;
    private EventShop eventShop;
    private final Catalogue catalogue;
    public ShopProtocol(final ProtocolContext ctx)
    {
        super(ProtocolID.Shop, ctx);
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.shipBlackList = new HashSet<>();
        this.consumableBlacklist = new HashSet<>();
        this.systemBlackList = new HashSet<>();
        setupSystemBlackList();
        setupShipBlackList();
        blacklistConsumables();
    }

    @Override
    public void injectUser(User user)
    {
        super.injectUser(user);

        this.shop = setupShop();
        /*
        if (gameServerParams.starterParams().testingMode())
        {
            setupEventShop();
        }
         */
        setupEventShop();
    }

    private void setupSystemBlackList()
    {
        //paints
        this.systemBlackList.add(226L);
        this.systemBlackList.add(227L);

    }
    private void blacklistConsumables()
    {
        consumableBlacklist.add(ResourceType.Uranium.guid);
        consumableBlacklist.add(ResourceType.YellowBox.guid);
        consumableBlacklist.add(ResourceType.GreenBox.guid);
        consumableBlacklist.add(ResourceType.RedBox.guid);
        consumableBlacklist.add(ResourceType.BlueBox.guid);

        /*
        if (gameServerParams.starterParams().testingMode())
        {
            //striker
            consumableBlacklist.add(ResourceType.Strikerx20Nuke.value);
            consumableBlacklist.add(ResourceType.Strikex5Nuke.value);

            //escort
            consumableBlacklist.add(ResourceType.Escortx20Nuke.value);
            consumableBlacklist.add(ResourceType.Escortx5Nuke.value);

            //liner
            consumableBlacklist.add(ResourceType.Linerx20Nuke.value);
            consumableBlacklist.add(ResourceType.Linerx5Nuke.value);
        }
         */
    }

    private void setupShipBlackList()
    {
        // if in test mode, do not add to blacklist
        if (!ctx.gameServerParams().starterParams().testingMode())
        {
            //brimir
            this.shipBlackList.add(98636899L); //obj key
            this.shipBlackList.add(3383345902L); //lvl 1
            this.shipBlackList.add(3765070071L); //lvl 2

            //surtur
            this.shipBlackList.add(3917123639L);
            this.shipBlackList.add(3535399470L);
            this.shipBlackList.add(114650019L);

            //stealth colonial
            this.shipBlackList.add(59555849L);
            this.shipBlackList.add(3210071581L);
            this.shipBlackList.add(2828347412L);

            //stealth cylon
            this.shipBlackList.add(113318329L);
            this.shipBlackList.add(266539981L);
            this.shipBlackList.add(4179783108L);
        }
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue((short) msgType);
        if (clientMessage == null)
            return;

        log.info("ClientMessage = {}", clientMessage);

        switch (clientMessage)
        {
            case EventShopItems ->
            {
                log.info("write event shop items to the client");
                user().send(writeEventShopItems(eventShop));
            }
            case Close ->
            {
                log.error(user().getPlayer().getPlayerLog() + "CLOSE SHOP NOT IMPLEMENTED");
            }
            case AllSales ->
            {
                user().send(writeSales());
                user().send(writeUpgradeSales());
            }
            case Items ->
            {
                try
                {
                    user().send(writeShopItems(shop));
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    illegalArgumentException.printStackTrace();
                }
                /*
                if (gameServerParams.starterParams().testingMode())
                {
                    user().send(writeEventShop(11133L, true));
                }
                 */
                user().send(writeEventShop(11133L, true));
            }

            default ->
            {
                log.error(user().getUserLog() + "ShopProtocol, case not implemented " + clientMessage);
            }
        }

    }

    private void setupEventShop()
    {
        final Player player = user().getPlayer();
        this.eventShop = player.getEventShop();
        eventShop.removeAllShipItems();


        List<ShipItem> items = new ArrayList<>();


        items.add(ShipSystem.fromGUID(227));
        items.add(ShipSystem.fromGUID(226));
        items.add(ItemCountable.fromGUID(ResourceType.YellowBox, 1));
        items.add(ItemCountable.fromGUID(ResourceType.GreenBox, 1));
        items.add(ItemCountable.fromGUID(ResourceType.RedBox, 1));

        eventShop.addShipItems(items);
    }

    private Shop setupShop()
    {
        final Player player = user().getPlayer();
        Shop shop = player.getShop();
        shop.removeAllShipItems();

        List<ShipSystemCard> allSystemCards = catalogue.getAllCardsOfView(CardView.ShipSystem);
        List<ShopItemCard> allAssociatedPriceCards = catalogue.getAllCardsOfView(CardView.Price);


        for (final ShopItemCard card : allAssociatedPriceCards)
        {
            final Price buyPrice = card.getBuyPrice();
            if (buyPrice.isEmpty())
                continue;


            //if (gameServerParams.starterParams().isLive() && (card.getTier() == 4) && !this.user().getPlayer().getBgoAdminRoles().hasRole(BgoAdminRoles.Developer))
            //    continue;

            if (card.getShopCategory() == ShopCategory.Ship)
            {
                //the ships that can be bought
                if (!this.shipBlackList.contains(card.getCardGuid()))
                {
                    shop.addShipItem(new StoreShip(card.getCardGuid(), 0));
                }
            }
            if (card.getShopCategory() == ShopCategory.Consumable || card.getShopCategory() == ShopCategory.Resource ||
                    card.getShopCategory() == ShopCategory.Augment)
            {
                if (!consumableBlacklist.contains(card.getCardGuid()))
                    shop.addShipItem(ItemCountable.fromGUID(card.getCardGuid(), 1));
            }
        }

        for (final ShipSystemCard sysCard : allSystemCards)
        {
            //if live mode do not publish t4 items if non-admin
            /*
            if (gameServerParams.starterParams().isLive() && (sysCard.getTier() == 4) && !this.user().getPlayer().getBgoAdminRoles().hasRole(BgoAdminRoles.Developer))
                continue;
             */

            if (systemBlackList.contains(sysCard.getCardGuid()))
                continue;

            final Optional<Card> existingPriceCard = catalogue.fetchCard(sysCard.getCardGuid(), CardView.Price);
            if (existingPriceCard.isEmpty())
                continue;

            // ???
            if (sysCard.getCardGuid() == 225 && !user().getPlayer().getBgoAdminRoles().hasOneRole(BgoAdminRoles.Developer, BgoAdminRoles.CommunityManager))
            {
                continue;
            }

            if (sysCard.getLevel() == 1 || sysCard.getShipSlotType() == ShipSlotType.avionics)
            {
                if (sysCard.getShipSlotType() == ShipSlotType.avionics && hasAlreadyInHoldLockerSlot(sysCard.getCardGuid(), user()))
                    continue;

                shop.addShipItem(ShipSystem.fromGUID(sysCard.getCardGuid()));
            }
            if (ctx.gameServerParams().starterParams().testingMode())
            {
                if (sysCard.getLevel() == 10)
                {
                    shop.addShipItem(ShipSystem.fromGUID(sysCard.getCardGuid()));
                }
                if (sysCard.getLevel() == 15)
                {
                    shop.addShipItem(ShipSystem.fromGUID(sysCard.getCardGuid()));
                }
            }
        }
        return shop;
    }

    public boolean hasAlreadyInHoldLockerSlot(final long itemGUID, final User user)
    {
        final Hold hold = user().getPlayer().getHold();
        final Locker locker = user().getPlayer().getLocker();


        final Optional<ShipItem> contains = hold.getByGUID(itemGUID);
        if (contains.isPresent())
            return true;
        final Optional<ShipItem> containsLocker = locker.getByGUID(itemGUID);
        if (containsLocker.isPresent())
            return true;

        final Hangar hangar = user().getPlayer().getHangar();
        final List<HangarShip> allShips = hangar.getAllHangarShips();
        for (final HangarShip ship : allShips)
        {
            for (ShipSlot slot : ship.getShipSlots().values())
            {
                if (slot.getShipSystem() != null && slot.getShipSystem().getCardGuid() == itemGUID)
                    return true;
            }
        }
        return false;
    }

    /**
     * Setups an item list of all reduced items
     * @return buffer
     */
    private BgoProtocolWriter writeSales()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Sales.shortValue);
        bw.writeLength(0);
        return bw;
    }

    private BgoProtocolWriter writeShopItems(final Shop shop)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Items.shortValue);
        bw.writeDesc(shop);

        return bw;
    }

    /**
     * Setups an item list of all reduced items
     * @return buffer
     */
    private BgoProtocolWriter writeUpgradeSales()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.UpgradeSales.shortValue);
        bw.writeLength(0); //no list to send so length is 0
        return bw;
    }

    public BgoProtocolWriter writeEventShopItems(final EventShop eventShop)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.EventShopItems.shortValue);

        bw.writeDesc(eventShop);

        return bw;
    }
    public BgoProtocolWriter writeEventShop(final long cardGuid, final boolean isActivated)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.EventShopAvailable.shortValue);
        bw.writeGUID(cardGuid);
        bw.writeBoolean(isActivated);
        return bw;
    }
}
