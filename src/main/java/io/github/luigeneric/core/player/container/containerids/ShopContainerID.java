package io.github.luigeneric.core.player.container.containerids;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainerID;

import java.io.IOException;

public class ShopContainerID extends IContainerID
{
    public ShopContainerID()
    {
        super(ContainerType.Shop);
    }

    @Override
    public void read(BgoProtocolReader br) throws IOException
    {

    }
}
