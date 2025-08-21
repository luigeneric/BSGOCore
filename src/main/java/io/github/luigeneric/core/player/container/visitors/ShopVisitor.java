package io.github.luigeneric.core.player.container.visitors;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ItemType;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.Price;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShopVisitor extends ContainerVisitor
{
    private final Shop fromShop;
    public ShopVisitor(final User user, final MoveItemParser moveItemParser, final BgoRandom bgoRandom)
    {
        super(user, moveItemParser, bgoRandom);
        //check is done in baseclass
        this.fromShop = this.user.getPlayer().getShop();
    }

    @Override
    public void visit(final Hold hold)
    {
        buyFromThisToContainer(hold);
    }



    @Override
    public void visit(final Locker locker)
    {
        buyFromThisToContainer(locker);
    }

    @Override
    public void visit(final ShipSlot shipSlot)
    {
        final Player player = user.getPlayer();
        if (player.getLocation().getGameLocation() != GameLocation.Room)
        {
            log.error("User with id " + player.getUserID() + " tried to move an item inside the slot while not in room!");
            return;
        }

        final ShipItem itemToBuy = fromShop.getByID(moveItemParser.getItemID());
        if (itemToBuy == null)
        {
            debugProtocol.sendEzMsg("item to buy is null, report this");
            return;
        }
        if (itemToBuy.getItemType() != ItemType.System)
        {
            debugProtocol.sendEzMsg("item to buy is not first system...report this");
            return;
        }

        final Price buyPrice = new Price();
        try
        {
            final boolean canBuy = checkItemToBuy(itemToBuy, player.getHold(), buyPrice, 1);
            if (!canBuy)
            {
                debugProtocol.sendEzMsg("tried to buy something while you cant .. ");
                return;
            }
            long buyCount = moveItemParser.getCount();
            this.removeBuyResources(buyPrice, player.getHold(), buyCount);
            final ShipItem copyItem = itemToBuy.copy();
            this.moveItemToSlotWithRemovalToOtherContainer((ShipSystem) copyItem, player.getHold(), shipSlot, ContainerType.Shop);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            log.warn(illegalArgumentException.getMessage());
        }
    }


    private void buyFromThisToContainer(final IContainer toContainer)
    {
        try
        {
            ShipItem itemToBuy = fromShop.getByID(moveItemParser.getItemID());
            final Price buyPrice = new Price();

            final long buyCount = moveItemParser.getCount();
            final var buyContainer = user.getPlayer().getHold();
            final boolean everythingOkay = checkItemToBuy(itemToBuy, buyContainer, buyPrice, buyCount);

            if (!everythingOkay)
            {
                log.warn("Could not buy item {} for {} {}", itemToBuy.getCardGuid(), buyPrice, buyCount);
                return;
            }

            log.info("User bought item {} with price {} and count {}", itemToBuy.toString(), buyPrice, buyCount);

            removeBuyResources(buyPrice, buyContainer, buyCount);
            itemToBuy = itemToBuy.copy();
            if (itemToBuy instanceof final ItemCountable buyCountable)
            {
                buyCountable.updateCount(buyCount);
            }

            this.addShipItem(itemToBuy, toContainer);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            log.error(Utils.getExceptionStackTrace(illegalArgumentException));
        }
    }
}
