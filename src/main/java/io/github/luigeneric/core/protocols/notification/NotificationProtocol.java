package io.github.luigeneric.core.protocols.notification;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.player.container.Locker;
import io.github.luigeneric.core.player.container.visitors.LockerVisitor;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.enums.JumpErrorReason;
import io.github.luigeneric.enums.JumpErrorSeverity;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.io.IOException;
import java.util.List;

public class NotificationProtocol extends BgoProtocol
{
    private final NotificationProtocolWriteOnly notificationProtocolWriteOnly;
    public NotificationProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Notification, ctx);
        this.notificationProtocolWriteOnly = new NotificationProtocolWriteOnly();
    }
    public NotificationProtocolWriteOnly writer()
    {
        return this.notificationProtocolWriteOnly;
    }

    @Override
    public void parseMessage(int msgType, BgoProtocolReader br) throws IOException
    {
        //this should never happen
        //TODO throw exception
    }
    /**
     * May be ShipSystem or ItemCountable!
     * @param itemList
     */
    public void sendAugmentItemsAndAdd(final List<ShipItem> itemList)
    {
        //send notification
        user().send(writer().writeAugmentItem(itemList));

        //first send the items into the hold
        final Locker locker = user().getPlayer().getLocker();
        final LockerVisitor visitor = new LockerVisitor(user(), null);
        for (final ShipItem shipItem : itemList)
        {
            visitor.addShipItem(shipItem, locker);
        }
    }

    public void sendJumpSectorNotAllowed()
    {
        final BgoProtocolWriter bw = writer().writeJumpNotification(JumpErrorSeverity.Error, JumpErrorReason.Closed);
        this.user().send(bw);
    }

}
