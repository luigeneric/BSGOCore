package io.github.luigeneric.core.player.container;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.container.containerids.ShopContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.util.ArrayList;
import java.util.List;

public class Shop extends ItemList
{
    public Shop(final List<ShipItem> items, final long userID)
    {
        super(new ShopContainerID(), items, userID);
    }
    public Shop(final long userID)
    {
        this(new ArrayList<>(), userID);
    }



    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
    }

    @Override
    public String toString()
    {
        return "Shop{" +
                "items=" + items +
                ", containerID=" + containerID +
                '}';
    }

    @Override
    public void accept(ContainerVisitor containerVisitor)
    {
        containerVisitor.visit(this);
    }

}
