package io.github.luigeneric.core.player.container.visitors;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.core.player.container.containerids.ShipSlotContainerID;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShipSlotVisitor extends ContainerVisitor
{
    private ShipSlot fromSlot;
    private HangarShip fromShip;

    public ShipSlotVisitor(User user, MoveItemParser moveItemParser)
    {
        super(user, moveItemParser, null);
    }
    private void setupHelperVars()
    {
        ShipSlot from = (ShipSlot) moveItemParser.getFrom();
        ShipSlotContainerID fromContainerID = (ShipSlotContainerID)from.getContainerID();
        //everything okay, check if char is null is done inside Baseclass
        this.fromShip = user.getPlayer().getHangar().getByServerId(fromContainerID.getShipID());
        this.fromSlot = fromShip.getShipSlots().getSlot(fromContainerID.getSlotID());
    }

    @Override
    public void visit(Shop shop)
    {
        setupHelperVars();
        final int itemID = moveItemParser.getItemID();
        sellItem(fromSlot, user.getPlayer().getHold(), itemID, 1);
    }


    @Override
    public void visit(Hold hold)
    {
        setupHelperVars();
        ShipItem removedSystem = fromSlot.removeShipItem(moveItemParser.getItemID());

        addShipItem(removedSystem, hold);

        user.send(playerProtocol.writer().writeShipSlots(fromShip));
    }

    @Override
    public void visit(Locker locker)
    {
        setupHelperVars();
        removeItemFromSlot(locker, this.fromSlot);
    }

    @Override
    public void visit(final ShipSlot shipSlot)
    {
        setupHelperVars();
        //client.send(debugProtocol.sendMsg("Due to first bug disabled for now"));
        final ShipItem itemToMove = fromSlot.getByID(moveItemParser.getItemID());

        //remove the item from from-Slot
        final ShipItem fromItem = fromSlot.removeShipItem(fromSlot.getShipSystem().getServerID());

        //there is still another system installed
        if (shipSlot.getShipSystem().getShipSystemCard() != null)
        {
            ShipItem removedItem = shipSlot.removeShipItem(itemToMove.getServerID());

            shipSlot.addShipItem(fromItem);
            fromSlot.addShipItem(removedItem);
        }
        else
        {
            try
            {
                shipSlot.addShipItem(itemToMove);
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                log.error("ShipSlotVisitor error! " + this.user.getPlayer().getPlayerLog() + " " + Utils.getExceptionStackTrace(illegalArgumentException));
                debugProtocol.sendEzMsg("ShipSlotError: " + illegalArgumentException.getMessage() + " please report this");
            }
        }

        //check is done in baseclass
        final HangarShip hangarShip = user.getPlayer().getHangar().getActiveShip();
        user.send(playerProtocol.writer().writeShipSlots(hangarShip));
    }
}
