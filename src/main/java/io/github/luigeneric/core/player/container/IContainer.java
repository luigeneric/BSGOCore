package io.github.luigeneric.core.player.container;



import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.util.Objects;
import java.util.Set;

public abstract class IContainer implements IProtocolWrite
{
    protected IContainerID containerID;

    public IContainer(final IContainerID containerID)
    {
        Objects.requireNonNull(containerID, "ContainerID cannot be null!");
        this.containerID = containerID;
    }

    public IContainerID getContainerID()
    {
        return this.containerID;
    }

    public void setContainerID(final IContainerID containerID)
    {
        this.containerID = containerID;
    }

    public abstract ShipItem getByID(final int id);

    public abstract Set<Integer> getAllItemsIDs();

    public abstract ShipItem removeShipItem(final int itemID);

    /**
     * Adds the given shipItem to the container.
     * @param shipItem the item to add
     * @return a reference to the existing item (in case it's a countable)
     */
    public abstract ShipItem addShipItem(final ShipItem shipItem);

    public abstract void accept(final ContainerVisitor containerVisitor);
}
