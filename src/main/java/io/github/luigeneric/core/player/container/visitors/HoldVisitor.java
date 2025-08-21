package io.github.luigeneric.core.player.container.visitors;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ItemType;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.utils.BgoRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * ContainerVisitor for if the source is the hold container
 */
@Slf4j
public class HoldVisitor extends ContainerVisitor
{
    private final Hold fromHold;

    public HoldVisitor(final User user, final MoveItemParser moveItemParser, final BgoRandom bgoRandom)
    {
        super(user, moveItemParser, bgoRandom);
        this.fromHold = this.user.getPlayer().getHold();
    }
    public HoldVisitor(final User user, final BgoRandom bgoRandom)
    {
        this(user, null, bgoRandom);
    }
    public HoldVisitor(final User user)
    {
        this(user, null, null);
    }


    @Override
    public void visit(final Hold hold)
    {
        debugProtocol.sendEzMsg("Item move from hold to hold..");
    }

    @Override
    public void visit(final Locker locker)
    {
        //should never be used
        final boolean isEquip = moveItemParser.isEquip();
        if (isEquip)
        {
            debugProtocol.sendEzMsg("Error in isEquip from Hold to Locker, report this error " + this.user.getPlayer().getUserID());
            return;
        }


        final ShipItem itemToMove = fromHold.getByID(moveItemParser.getItemID());
        if (itemToMove == null) throw new IllegalArgumentException(EXCEPT_MSG_CANNOT_FIND + " " + moveItemParser.getItemID());


        if (itemToMove.getItemType() == ItemType.Countable)
        {
            moveCountable((ItemCountable) itemToMove, fromHold, locker);
        }
        else
        {
            moveShipItem(itemToMove, this.fromHold, locker);
        }
    }

    @Override
    public void visit(final ShipSlot shipSlot)
    {
        fromContainerToSlot(fromHold, shipSlot);
    }


    @Override
    public void visit(final Shop shop)
    {
        sellItem(fromHold, fromHold, moveItemParser.getItemID(), moveItemParser.getCount());
    }

    @Override
    public void visit(final BlackHole blackHole)
    {
        final ShipItem itemToRemove = fromHold.getByID(moveItemParser.getItemID());
        if (itemToRemove == null)
        {
            log.warn("Move item from Hold to Blackhole but was not present!");
            return;
        }
        log.info("User {} threw item {} into blackhole!", user.getUserLog(), itemToRemove.getCardGuid());
        removeShipItem(itemToRemove, fromHold);

        blackHole.addShipItem(itemToRemove);
    }
}
