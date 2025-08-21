package io.github.luigeneric.core.player.container.containerids;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainerID;

import java.io.IOException;

public class EventShopContainerID extends IContainerID
{
    public EventShopContainerID()
    {
        super(ContainerType.EventShop);
    }

    @Override
    public void read(BgoProtocolReader br) throws IOException
    {

    }
}
