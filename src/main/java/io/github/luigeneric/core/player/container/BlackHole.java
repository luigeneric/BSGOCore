package io.github.luigeneric.core.player.container;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.container.containerids.BlackHoleContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.templates.shipitems.ShipItem;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class BlackHole extends IContainer
{
    public BlackHole()
    {
        super(new BlackHoleContainerID());
    }


    @Override
    public ShipItem getByID(int id)
    {
        throw new IllegalCallerException();
    }

    @Override
    public Set<Integer> getAllItemsIDs()
    {
        throw new IllegalCallerException();
    }

    @Override
    public ShipItem removeShipItem(int itemID)
    {
        throw new IllegalCallerException();
    }

    @Override
    public ShipItem addShipItem(ShipItem shipItem)
    {
        shipItem.setServerID(0);
        return shipItem;
    }

    @Override
    public void accept(final ContainerVisitor containerVisitor)
    {
        containerVisitor.visit(this);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        log.error("BlackHole write but not implemented!");
    }


}
