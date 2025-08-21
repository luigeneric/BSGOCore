package io.github.luigeneric.core.player.container.visitors;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.utils.BgoRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LockerVisitor extends ContainerVisitor
{
    private final Locker fromLocker;
    public LockerVisitor(User user, MoveItemParser moveItemParser, final BgoRandom bgoRandom)
    {
        super(user, moveItemParser, bgoRandom);
        this.fromLocker = user.getPlayer().getLocker();
    }
    public LockerVisitor(final User user, final BgoRandom bgoRandom)
    {
        this(user, null, bgoRandom);
    }

    @Override
    public void visit(final Hold hold)
    {
        //from locker to hold is simple just the full item
        final ShipItem itemToMove = fromLocker.getByID(moveItemParser.getItemID());
        if (itemToMove == null)
        {
            log.error(user.getUserLog() + " moved from locker to hold but item was null " + EXCEPT_MSG_CANNOT_FIND + " " + moveItemParser.getItemID() + " " +
                    user.getUserLog());
            return;
        }

        moveShipItem(itemToMove, fromLocker, hold);
    }

    @Override
    public void visit(final Locker locker)
    {
        //do nothing or throw exception
        ///TODO this can never happen -> hack, ban
    }

    @Override
    public void visit(final ShipSlot shipSlot)
    {
        fromContainerToSlot(fromLocker, shipSlot);
    }

    @Override
    public void visit(final Shop shop)
    {
        sellItem(fromLocker, fromLocker, moveItemParser.getItemID(), moveItemParser.getCount());
    }

    @Override
    public void visit(BlackHole blackHole)
    {
        final ShipItem itemToRemove = fromLocker.getByID(moveItemParser.getItemID());
        if (itemToRemove == null)
        {
            log.warn(user.getUserLog() + "Move item from Hold to Blackhole but was not present!");
            return;
        }

        removeShipItem(itemToRemove, fromLocker);

        blackHole.addShipItem(itemToRemove);
    }
}
