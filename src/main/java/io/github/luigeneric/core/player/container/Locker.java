package io.github.luigeneric.core.player.container;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.container.containerids.LockerContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.core.protocols.player.ServerMessage;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.util.ArrayList;
import java.util.List;

public class Locker extends ItemList
{
    public Locker(final List<ShipItem> items, final long userID)
    {
        super(new LockerContainerID(), items, userID);
    }

    public Locker(final long userID)
    {
        this(new ArrayList<>(), userID);
    }

    @Override
    public void accept(ContainerVisitor containerVisitor)
    {
        containerVisitor.visit(this);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeMsgType(ServerMessage.LockerItems.getValue());
        super.write(bw);
    }

    @Override
    public String toString()
    {
        return "Locker{" +
                "containerID=" + containerID +
                '}';
    }
}
