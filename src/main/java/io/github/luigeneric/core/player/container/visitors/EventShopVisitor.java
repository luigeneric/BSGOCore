package io.github.luigeneric.core.player.container.visitors;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.utils.Price;
import io.github.luigeneric.utils.BgoRandom;
import io.github.luigeneric.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventShopVisitor extends ContainerVisitor
{
    private final EventShop fromEventShop;

    public EventShopVisitor(final User user,
                            final MoveItemParser moveItemParser,
                            final BgoRandom bgoRandom
                            )
    {
        super(user, moveItemParser, bgoRandom);
        this.fromEventShop = user.getPlayer().getEventShop();
    }

    @Override
    public void visit(Hold hold)
    {
        log.info("move item from event shop to hold");
        try
        {
            ShipItem itemToBuy = fromEventShop.getByID(moveItemParser.getItemID());
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

            this.addShipItem(itemToBuy, hold);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            log.error(Utils.getExceptionStackTrace(illegalArgumentException));
        }
    }

    @Override
    public void visit(Locker locker)
    {
        super.visit(locker);
        log.warn("Cheat warning, eventshop movement from eventshop to {} from user={}", locker.getContainerID().getContainerType(), user.getUserLog());
    }

    @Override
    public void visit(ShipSlot shipSlot)
    {
        super.visit(shipSlot);
        log.warn("Cheat warning, eventshop movement from eventshop to {} from user={}", shipSlot.getContainerID().getContainerType(), user.getUserLog());
    }

    @Override
    public void visit(Shop shop)
    {
        super.visit(shop);
        log.warn("Cheat warning, eventshop movement from eventshop to {} from user={}", shop.getContainerID().getContainerType(), user.getUserLog());
    }

    @Override
    public void visit(Mail.MailContainer mailContainer)
    {
        super.visit(mailContainer);
        log.warn("Cheat warning, eventshop movement from eventshop to {} from user={}", mailContainer.getContainerID().getContainerType(), user.getUserLog());
    }

    @Override
    public void visit(BlackHole blackHole)
    {
        super.visit(blackHole);
    }

    @Override
    public void visit(EventShop eventShop)
    {
        super.visit(eventShop);
    }
}
