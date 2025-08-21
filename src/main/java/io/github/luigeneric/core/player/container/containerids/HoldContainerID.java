package io.github.luigeneric.core.player.container.containerids;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainerID;

import java.io.IOException;

public class HoldContainerID extends IContainerID
{
    public HoldContainerID()
    {
        super(ContainerType.Hold);
    }

    @Override
    public void read(BgoProtocolReader br) throws IOException
    {

    }
}
