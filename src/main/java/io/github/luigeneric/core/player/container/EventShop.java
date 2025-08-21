package io.github.luigeneric.core.player.container;

import io.github.luigeneric.core.player.container.containerids.EventShopContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;

public class EventShop extends ItemList
{
    public EventShop(long userID)
    {
        super(new EventShopContainerID(), userID);
    }

    @Override
    public String toString()
    {
        return "EventShop{" +
                "items=" + items +
                ", readWriteLock=" + readWriteLock +
                ", containerID=" + containerID +
                '}';
    }

    @Override
    public void accept(ContainerVisitor containerVisitor)
    {
        containerVisitor.visit(this);
    }
}
