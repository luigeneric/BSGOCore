package io.github.luigeneric.core.player.container;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.container.containerids.HoldContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.core.protocols.player.ServerMessage;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.util.ArrayList;
import java.util.List;

public class Hold extends ItemList
{
    public Hold(final List<ShipItem> items, final long userID)
    {
        super(new HoldContainerID(), items, userID);
    }

    public Hold(final long userID)
    {
        this(new ArrayList<>(), userID);
    }

    @Override
    public void accept(final ContainerVisitor containerVisitor)
    {
        containerVisitor.visit(this);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeMsgType(ServerMessage.HoldItems.getValue());
        super.write(bw);
    }

    @Override
    public String toString()
    {
        return "Hold{" +
                "containerID=" + containerID +
                '}';
    }
}
