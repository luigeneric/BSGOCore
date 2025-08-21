package io.github.luigeneric.core.player.container.containerids;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.IProtocolRead;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainerID;

import java.io.IOException;

public class MailContainerID extends IContainerID implements IProtocolRead
{
    private int mailID;

    public MailContainerID()
    {
        super(ContainerType.Mail);
    }

    @Override
    public void read(final BgoProtocolReader br) throws IOException
    {
        this.mailID = br.readUint16();
    }

    public int getMailID()
    {
        return mailID;
    }
}
