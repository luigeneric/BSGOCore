package io.github.luigeneric.core.player.container.visitors;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.Hangar;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.core.player.container.containerids.ShipSlotContainerID;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.debug.DebugProtocol;
import io.github.luigeneric.core.protocols.notification.NotificationProtocolWriteOnly;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipConsumableCard;
import io.github.luigeneric.templates.cards.ShipSystemCard;
import io.github.luigeneric.templates.cards.ShopItemCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.*;
import io.github.luigeneric.templates.utils.AugmentActionType;
import io.github.luigeneric.templates.utils.Price;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.*;

@Slf4j
public abstract class ContainerVisitor
{
    public static final String EXCEPT_MSG_CANNOT_FIND = "Cannot find itemID";
    public static final String EXCEPT_MSG_CANNO_SELL = "Cannot sell item";

    protected final User user;
    protected final MoveItemParser moveItemParser;
    protected final PlayerProtocol playerProtocol;
    protected final DebugProtocol debugProtocol;
    protected final BgoRandom bgoRandom;
    protected final Catalogue catalogue;

    public ContainerVisitor(final User user, final MoveItemParser moveItemParser, final BgoRandom bgoRandom)
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.user = user;
        this.moveItemParser = moveItemParser;
        this.playerProtocol = this.user.getProtocol(ProtocolID.Player);
        this.debugProtocol = this.user.getProtocol(ProtocolID.Debug);
        this.bgoRandom = Objects.requireNonNullElseGet(bgoRandom, BgoRandom::new);

        MDC.put("userID", String.valueOf(user.getPlayer().getUserID()));
    }

    public ContainerVisitor(final User user, final BgoRandom bgoRandom)
    {
        this(user, null, bgoRandom);
    }

    protected void removeShipItem(final ShipItem shipItem, IContainer fromContainer)
    {
        fromContainer.removeShipItem(shipItem.getServerID());
        user.send(playerProtocol.writeRemoveItem(fromContainer, shipItem.getServerID()));
    }

    public void addShipItem(final ShipItem shipItem, IContainer toContainer)
    {
        addShipItem(this.user, shipItem, toContainer);
    }
    public static void addShipItem(final User client, final ShipItem shipItem, final IContainer toContainer)
    {
        if (shipItem == null)
            return;

        if (shipItem instanceof ItemCountable countable && countable.getCount() == 0)
        {
            return;
        }

        final ShipItem existingItemOrNewItem = toContainer.addShipItem(shipItem);
        final PlayerProtocol playerProtocol = client.getProtocol(ProtocolID.Player);
        client.send(playerProtocol.writeAddItem(toContainer, existingItemOrNewItem));
    }
    protected void moveShipItem(final ShipItem shipItem, IContainer from, IContainer toContainer)
    {
        //first remove
        this.removeShipItem(shipItem, from);

        //add to new container
        this.addShipItem(shipItem, toContainer);
    }
    protected void fromContainerToSlot(final IContainer fromContainer, final ShipSlot targetSlot)
    {
        final Player player = user.getPlayer();
        if (player.getLocation().getGameLocation() != GameLocation.Room)
        {
            log.error("User {} tried to move an item inside the slot while not in room!", user.getUserLog());
            return;
        }

        final ShipItem itemToMove = fromContainer.getByID(moveItemParser.getItemID());
        if (itemToMove == null)
        {
            log.warn("Critical servererror! (but no crash), move item but can't find desired id {} player: {}",
                    moveItemParser.getItemID(),
                    player.getPlayerLog()
            );
            return;
        }
        if (!(itemToMove instanceof ShipSystem shipSystemToMove))
        {
            log.warn("Critical, itemToMove is not of type system but {}", itemToMove.getItemType());
            debugProtocol.sendEzMsg("error_item_to_slot_L1, report this");
            return;
        }
        //check if system has any restrictions
        final ShipSlotContainerID shipSlotContainerID = (ShipSlotContainerID) targetSlot.getContainerID();
        final Hangar hangar = user.getPlayer().getHangar();
        final HangarShip activeShip = hangar.getActiveShip();
        final boolean isActiveShip = activeShip.getServerId() == shipSlotContainerID.getShipID();
        if (!isActiveShip)
        {
            log.warn("Cheat: user({}) moved item from {} to {} but was not active ship!",
                    user.getUserLog(),
                    fromContainer.getContainerID(),
                    shipSlotContainerID
            );
            return;
        }
        final boolean isBlocked = shipSystemToMove.getShipSystemCard()
                .isObjectKeyRestrictionsBlocked(activeShip.getShipCard().getShipObjectKey());
        if (isBlocked)
        {
            log.warn("Moved item into slot even if it's blocked! {} {} {}",
                    user.getUserLog(),
                    shipSystemToMove.getShipSystemCard(),
                    activeShip.getShipCard()
            );
            return;
        }

        final ShipSystem currentSlotSystem = targetSlot.getShipSystem();
        if (currentSlotSystem.getShipSystemCard() != null)
        {
            removeItemFromSlot(fromContainer, targetSlot);
        }

        log.info("User {} move item from {} to slot {}",
                user.getUserLogSimple(),
                fromContainer.getContainerID().getContainerType(),
                targetSlot.getContainerID()
        );

        this.moveItemToSlot(shipSystemToMove, fromContainer, targetSlot);
    }

    protected void moveItemToSlot(final ShipSystem itemToMove, final ShipSlot shipSlot)
    {
        final ShipSlotContainerID shipSlotContainerID = (ShipSlotContainerID)shipSlot.getContainerID();
        final HangarShip hangarShip = user.getPlayer().getHangar().getByServerId(shipSlotContainerID.getShipID());
        shipSlot.addShipItem(itemToMove);

        user.send(playerProtocol.writer().writeShipSlots(hangarShip));
        hangarShip.getShipStats().applyStats();
    }


    protected void moveItemToSlot(ShipSystem itemToMove, IContainer from, ShipSlot shipSlot)
    {
        this.removeShipItem(itemToMove, from);
        final ShipSlotContainerID shipSlotContainerID = (ShipSlotContainerID)shipSlot.getContainerID();
        final HangarShip hangarShip = user.getPlayer().getHangar().getByServerId(shipSlotContainerID.getShipID());
        shipSlot.addShipItem(itemToMove);

        user.send(playerProtocol.writer().writeShipSlots(hangarShip));
        hangarShip.getShipStats().applyStats();
    }


    protected void moveItemToSlotWithRemovalToOtherContainer(final ShipSystem itemToMove, final ItemList other,
                                                             final ShipSlot shipSlot, final ContainerType casterType)
    {
        final ShipSystem currentSlotSystem = shipSlot.getShipSystem();
        if (currentSlotSystem.getShipSystemCard() != null)
        {
            this.removeItemFromSlot(other, shipSlot);
        }

        if (casterType == ContainerType.Shop)
        {
            this.moveItemToSlot(itemToMove, shipSlot);
        }
        else
        {
            this.moveItemToSlot(itemToMove, other, shipSlot);
        }
    }


    protected ShipSystem removeItemFromSlot(final ShipSlot shipSlot)
    {
        final HangarShip activeShip = user.getPlayer().getHangar().getActiveShip();
        final ShipSlotContainerID shipSlotContainerID = (ShipSlotContainerID) shipSlot.getContainerID();
        if (activeShip.getServerId() != shipSlotContainerID.getShipID())
        {
            //error
            debugProtocol.sendEzMsg("error in removeItemFromSlot, report this");
            return null;
        }

        final ShipSystem existingSystem = shipSlot.getShipSystem();
        final ShipItem removedItem = shipSlot.removeShipItem(existingSystem.getServerID());
        user.send(playerProtocol.writer().writeShipSlots(activeShip));
        return (ShipSystem) removedItem;
    }

    protected void removeItemFromSlot(final IContainer to, final ShipSlot shipSlot)
    {
        var activeShip = user.getPlayer().getHangar().getActiveShip();
        ShipSlotContainerID shipSlotContainerID = (ShipSlotContainerID) shipSlot.getContainerID();
        if (activeShip.getServerId() != shipSlotContainerID.getShipID())
        {
            //error
            debugProtocol.sendEzMsg("error in removeItemFromSlot, report this");
            return;
        }

        final ShipSystem shipSystem = shipSlot.getShipSystem();
        shipSlot.removeShipItem(shipSystem.getServerID());
        //moved item to other container
        this.addShipItem(shipSystem, to);
        //send the slot is free

        user.send(playerProtocol.writer().writeShipSlots(activeShip));
    }

    public static boolean isEnoughInContainer(final Price price, final ItemList container, final long buyCount)
    {
        for (final Map.Entry<Long, Float> entry : price.getItems().entrySet())
        {
            final long priceCount = (long) Math.ceil(entry.getValue() * buyCount);

            final Optional<ItemCountable> optCountable = container.hasItemCountable(entry.getKey());
            if (optCountable.isPresent())
            {
                final ItemCountable exCountable = optCountable.get();
                if (exCountable.getCount() < priceCount)
                {
                    return false;
                }
            }
            else
            {
                //if price is there but count is 0, it's still okay
                return priceCount == 0;
            }
        }
        return true;
    }

    /**
     * Reduces item countable by a non-negative number count
     * @param resourceType guid of the item
     * @param count value
     * @return true if reduce process was successfully
     * @throws IllegalArgumentException if count is higher than the current item count is available
     */
    public boolean reduceItemCountableByCount(final ResourceType resourceType, final long count) throws IllegalArgumentException
    {
        return reduceItemCountableByCount(resourceType.guid, count);
    }

    /**
     * Reduces item countable by a non-negative number count
     * @param itemCountableGUID guid of the item
     * @param count value
     * @return true if reduce process was successfully
     * @throws IllegalArgumentException if count is higher than the current item count is available
     */
    public boolean reduceItemCountableByCount(final long itemCountableGUID, final long count) throws IllegalArgumentException
    {
        final Hold hold = user.getPlayer().getHold();
        final Optional<ShipItem> optItem = hold.getByGUID(itemCountableGUID);
        if (optItem.isEmpty())
            return false;

        try
        {
            reduceItemCountableByCount((ItemCountable) optItem.get(), count, hold);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        return true;
    }
    public void reduceItemCountableByCount(final ItemCountable currentItemConsumable, final long count,
                                           final IContainer fromContainer) throws IllegalArgumentException
    {
        if (currentItemConsumable.getCount() < count)
            throw new IllegalArgumentException("The ItemCountable is less than the count to be decreased!");


        final long newCount = currentItemConsumable.getCount() - count;

        //decrease item
        currentItemConsumable.updateCount(newCount);


        if (newCount > 0)
        {
            user.send(playerProtocol.writeAddItem(fromContainer, currentItemConsumable));
        }
        else
        {
            this.removeShipItem(currentItemConsumable, fromContainer);
        }
    }
    public void removeBuyResources(final Price price, final ItemList buyContainer, final long buyCount)
    {
        for (Map.Entry<Long, Float> entry : price.getItems().entrySet())
        {
            final Optional<ShipItem> optItem = buyContainer.getByGUID(entry.getKey());
            if (optItem.isEmpty())
                continue;

            if (optItem.get() instanceof ItemCountable exCountable)
            {
                //itemPrice * buyCount
                final long sumCount = (long) Math.ceil(entry.getValue() * buyCount);
                this.reduceItemCountableByCount(exCountable, sumCount, buyContainer);
            }
        }
    }

    public boolean upgradeSystem(final IContainer itemContainerSource,
                                 final Hold hold, final ShipSystem shipSystemToUpgrade, final int newLevel)
    {
        //get accumulated prices
        final Price accumulatedPrice = new Price();
        ShipSystemCard tmpSystemCard = shipSystemToUpgrade.getShipSystemCard();
        int tmpLevel = shipSystemToUpgrade.getShipSystemCard().getLevel();
        while (tmpLevel < newLevel)
        {

            if (tmpSystemCard == null) return false;
            final Optional<ShopItemCard> tmpShopCard = catalogue.fetchCard(tmpSystemCard.getCardGuid(), CardView.Price);
            if (tmpShopCard.isEmpty()) return false;

            final Price tmpUpgradePrice = tmpShopCard.get().getUpgradePrice();
            if (tmpUpgradePrice == null) return false;

            accumulatedPrice.addPrice(tmpUpgradePrice);

            final Optional<ShipSystemCard> optSystemCard = catalogue.fetchCard(tmpSystemCard.getNextCardGuid(), CardView.ShipSystem);
            if (optSystemCard.isEmpty())
                return false;
            tmpSystemCard = optSystemCard.get();
            tmpLevel = tmpSystemCard.getLevel();
        }

        //remove the ressources if enough in Hold
        final boolean hasEnoughInContainer = isEnoughInContainer(accumulatedPrice, hold, 1);
        if (!hasEnoughInContainer)
            return false;

        this.removeBuyResources(accumulatedPrice, hold, 1);

        if (itemContainerSource.getContainerID().getContainerType() == ContainerType.ShipSlot)
        {
            final ShipSystem removedSystem = this.removeItemFromSlot((ShipSlot) itemContainerSource);
            if (removedSystem == null) return false;
            final ShipSystem upgradedSystem = ShipSystem.fromGUID(tmpSystemCard.getCardGuid());
            this.moveItemToSlot(upgradedSystem, (ShipSlot) itemContainerSource);
            return true;
        }
        //remove the item and add the new item with the level
        else
        {
            this.removeShipItem(shipSystemToUpgrade, itemContainerSource);
            final ShipSystem upgradedSystem = ShipSystem.fromGUID(tmpSystemCard.getCardGuid());
            upgradedSystem.setTimeOfLastUse(shipSystemToUpgrade.getTimeOfLastUseLocalDateTime());
            this.addShipItem(upgradedSystem, itemContainerSource);
            return true;
        }
    }

    public boolean upgradeSystemByPack(final IContainer itemContainerSource,
                                    final Hold hold, final ShipSystem shipSystemToUpgrade, final long packCount)
    {
        final Optional<ShopItemCard> optShopItemCard = catalogue.fetchCard(shipSystemToUpgrade.getCardGuid(), CardView.Price);
        if (optShopItemCard.isEmpty()) return false;

        final ShopItemCard shopItemCard = optShopItemCard.get();


        final float cubitsPrice = shopItemCard.getUpgradePrice().getItems().get(ResourceType.Cubits.guid);
        final float chanceToUpgrade = Mathf.min(1f, packCount / (cubitsPrice / 1000f));

        Optional<ShipItem> tuningKits = hold.getByGUID(ResourceType.TuningKit.guid);
        if (tuningKits.isEmpty()) return false;

        ShipItem tuningKitsItem = tuningKits.get();
        if (tuningKitsItem.getItemType() != ItemType.Countable) return false;
        ItemCountable tuningKitsCountable = (ItemCountable) tuningKitsItem;

        final Price price = new Price();
        price.addItem(tuningKitsCountable.getCardGuid(), packCount);

        final boolean isEnoughInHangar = isEnoughInContainer(price, hold, 1);
        if (!isEnoughInHangar) return false;

        this.removeBuyResources(price, hold, 1);

        final boolean success = this.bgoRandom.rollChance(chanceToUpgrade);

        final NotificationProtocolWriteOnly notificationProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Notification);
        user.send(notificationProtocolWriteOnly.writeSystemUpgradeResult(success));
        if (!success)
            return false;

        final ShipSystem upgradedSystem = ShipSystem.fromGUID(shipSystemToUpgrade.getShipSystemCard().getNextCardGuid());
        upgradedSystem.setServerID(shipSystemToUpgrade.getServerID());
        upgradedSystem.setTimeOfLastUse(shipSystemToUpgrade.getTimeOfLastUseLocalDateTime());



        if (itemContainerSource.getContainerID().getContainerType() == ContainerType.ShipSlot)
        {
            final ShipSystem removedSystem = this.removeItemFromSlot((ShipSlot) itemContainerSource);
            if (removedSystem == null) return false;
            this.moveItemToSlot(upgradedSystem, (ShipSlot) itemContainerSource);
            return true;
        }
        else
        {
            this.removeShipItem(shipSystemToUpgrade, itemContainerSource);
            this.addShipItem(upgradedSystem, itemContainerSource);
        }

        return true;
    }

    /**
     * Sells an Item with the itemID in 'from' and places the proceeds into proceedsContainer
     * @param from the container the items will be removed from
     * @param proceedsContainer the container to add the new items to
     * @param itemID the item-id
     * @param count the item-count of the item to move
     * @throws IllegalArgumentException if the item to sell cannot be found
     */
    protected void sellItem(final IContainer from, final ItemList proceedsContainer,
                            final int itemID, final long count) throws IllegalArgumentException
    {
        final ShipItem itemToSell = from.getByID(itemID);
        if (itemToSell == null)
            throw new IllegalArgumentException(EXCEPT_MSG_CANNOT_FIND);

        //get the sell prices
        final Optional<ShopItemCard> optShopItemCard = catalogue.fetchCard(itemToSell.getCardGuid(), CardView.Price);
        if (optShopItemCard.isEmpty())
        {
            log.warn("ShopItemCard is null for move item from hold to shop");
            return;
        }
        final ShopItemCard shopItemCard = optShopItemCard.get();

        //check if the item can be sold
        if (!shopItemCard.isCanBeSold())
        {
            log.warn("User cheat {}, tried to sell item which cannot be sold!", user.getUserLog());
            throw new IllegalArgumentException(EXCEPT_MSG_CANNO_SELL);
        }

        //get the reward items from selling
        final Price sellPrice = shopItemCard.getSellPrice();
        log.info("User {} sells item guid {} from {} for {} cnt {}",
                user.getUserLogSimple(),
                itemToSell.getCardGuid(),
                from.getContainerID().getContainerType(),
                sellPrice,
                count
                );



        //remove the item
        if (itemToSell instanceof ItemCountable itemCountable)
        {
            try
            {
                reduceItemCountableByCount(itemCountable, count, from);
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                log.warn("Cheat, user tried to sell more than in the selling container, item: {} cnt: {}", itemCountable, count);
                return;
            }
        }
        else
        {
            if (from.getContainerID().getContainerType() == ContainerType.ShipSlot)
            {
                removeItemFromSlot((ShipSlot) from);
            }
            else
            {
                removeShipItem(itemToSell, from);
            }
        }

        final List<ShipItem> sellItems = getSellItems(sellPrice, count);
        for(ShipItem item : sellItems)
        {
            addShipItem(item, proceedsContainer);
        }
    }

    protected void moveCountable(final ItemCountable itemCountable, final IContainer from, final IContainer toContainer)
    {
        //first remove from startcontainer
        final long countToMove = this.moveItemParser.getCount();

        //should never happen unless the client got modified
        if (countToMove > itemCountable.getCount())
        {
            log.error("Attempt to move more of first countable than present " + user.getUserLog());
        } else if (countToMove == itemCountable.getCount())
        {
            moveShipItem(itemCountable, from, toContainer);
        } else
        {
            itemCountable.decrementCount(countToMove);
            user.send(playerProtocol.writeAddItem(from, itemCountable));


            ItemCountable targetCountable = ItemCountable.fromGUID(itemCountable.getCardGuid(), countToMove);

            final ShipItem existingCountable = toContainer.addShipItem(targetCountable);
            user.send(playerProtocol.writeAddItem(toContainer, existingCountable));
        }
    }

    /**
     * Buy stuff directed into hold
     * @param sellPrices first single sell price
     * @param count how many times this will be sold
     * @return
     */
    protected static List<ShipItem> getSellItems(final Price sellPrices, final long count)
    {
        final List<ShipItem> sellItems = new ArrayList<>();
        for (Map.Entry<Long, Float> entry : sellPrices.getItems().entrySet())
        {
            final long priceGUID = entry.getKey();

            try
            {
                final ShipItem sellItem = ShipItemFactory.createFromGuid(priceGUID);

                if (sellItem instanceof ItemCountable itemCountable)
                {
                    itemCountable.updateCount((long) (entry.getValue() * count));
                    sellItems.add(itemCountable);
                }
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                illegalArgumentException.printStackTrace();
            }
        }
        return sellItems;
    }

    public boolean useAugment()
    {
        final int itemID = this.moveItemParser.getItemID();
        final IContainer from = this.moveItemParser.getFrom();

        final ShipItem existingItem = from.getByID(itemID);
        if (existingItem == null)
            return false;
        if (existingItem.getItemType() != ItemType.Countable)
            return false;

        if (!(existingItem instanceof ItemCountable existingCountable))
        {
            return false;
        }
        final Optional<ShipConsumableCard> optAugmentItem = catalogue.fetchCard(existingCountable.getCardGuid(), CardView.ShipConsumable);
        if (optAugmentItem.isEmpty())
            return false;
        final ShipConsumableCard augmentItem = optAugmentItem.get();
        if (augmentItem.getAugmentActionType() != AugmentActionType.None)
            return false;

        //remove item
        reduceItemCountableByCount(existingCountable, 1, from);

        return true;
    }
    public boolean augmentMassActivationIsFineAndRemove(final long iterations)
    {
        //check if to analyse item is present
        final int itemID = this.moveItemParser.getItemID();
        final IContainer from = this.moveItemParser.getFrom();

        final ShipItem existingItem = from.getByID(itemID);
        if (existingItem == null)
            return false;
        if (existingItem.getItemType() != ItemType.Countable)
            return false;

        final ItemCountable toAnalyseItem = (ItemCountable) existingItem;
        if (iterations > toAnalyseItem.getCount())
            return false;
        final Optional<ShipConsumableCard> optToAnalyseCard = catalogue.fetchCard(toAnalyseItem.getCardGuid(), CardView.ShipConsumable);
        if (optToAnalyseCard.isEmpty())
            return false;
        final ShipConsumableCard toAnalyseCard = optToAnalyseCard.get();
        if (toAnalyseCard.getAugmentActionType() != AugmentActionType.LootItem)
            return false;

        //check has enough techn analayse tools
        final Hold hold = this.user.getPlayer().getHold();
        final Optional<ItemCountable> hasTechAnKits = hold.hasItemCountable(ResourceType.TechnicalAnalysisKit.guid);
        if (hasTechAnKits.isEmpty())
            return false;

        final ItemCountable techAnalysisKits = hasTechAnKits.get();
        if (iterations > techAnalysisKits.getCount())
            return false;

        //remove both items
        reduceItemCountableByCount(toAnalyseItem, iterations, from);
        reduceItemCountableByCount(techAnalysisKits, iterations, hold);

        return true;
    }


    /**
     * Checks if the item can be bought using the buyContainer
     * @param itemToBuy the item to buy
     * @param buyContainer buy container is Hold by default
     * @param buyPrice filled using call by reference
     * @param buyCount counter to buy the given item
     * @return true if the item can be bought, false otherwise
     */
    protected boolean checkItemToBuy(final ShipItem itemToBuy, final ItemList buyContainer, final Price buyPrice, final long buyCount)
    {
        if (itemToBuy == null) throw new IllegalArgumentException(EXCEPT_MSG_CANNOT_FIND + " " + moveItemParser.getItemID());
        if (buyPrice == null) throw new IllegalArgumentException("BuyPrice is call by reference filled, but was null");

        //get prices
        final Optional<ShopItemCard> optShopItemCard = CDI.current().select(Catalogue.class).get().fetchCard(itemToBuy.getCardGuid(), CardView.Price);
        if (optShopItemCard.isEmpty())
        {
            DebugProtocol debugProtocol = user.getProtocol(ProtocolID.Debug);
            debugProtocol.sendEzMsg("ShopItemCard for this item is not present! Please inform me..");
            return false;
        }

        //check prices
        final Price tmpPrice = optShopItemCard.get().getBuyPrice();
        boolean isEnoughInContainer = isEnoughInContainer(tmpPrice, buyContainer, buyCount);
        if (!isEnoughInContainer) return false;
        buyPrice.addPrice(tmpPrice);

        return true;
    }

    public void visit(final Hold hold)
    {
        debugProtocol.sendEzMsg("not implemented");
    }
    public void visit(final Locker locker)
    {
        debugProtocol.sendEzMsg("not implemented");
    }
    public void visit(final ShipSlot shipSlot)
    {
        debugProtocol.sendEzMsg("not implemented");
    }
    public void visit(final Shop shop)
    {
        debugProtocol.sendEzMsg("not implemented");
    }
    public void visit(final Mail.MailContainer mailContainer)
    {
        debugProtocol.sendEzMsg("not implemented");
    }
    public void visit(final BlackHole blackHole)
    {
        debugProtocol.sendEzMsg("not implemented");
    }

    public void visit(final EventShop eventShop)
    {
        debugProtocol.sendEzMsg("not implemented");
    }
}
