package io.github.luigeneric.core.player.container.containerids;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainerID;

import java.io.IOException;

public class BlackHoleContainerID extends IContainerID
{
    public BlackHoleContainerID()
    {
        super(ContainerType.BlackHole);
    }

    @Override
    public void read(BgoProtocolReader br) throws IOException
    {

    }
}
