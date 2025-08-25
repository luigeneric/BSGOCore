package io.github.luigeneric.core.protocols.sync;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.AbstractConnection;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;

import java.io.IOException;

public class SyncProtocol extends BgoProtocol
{
    public SyncProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Sync, ctx);
    }


    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        if (msgType == 0)
        {
            user().send(writeSyncReply());
        }
    }

    public BgoProtocolWriter writeSyncReply()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(1);
        final long millisec = System.currentTimeMillis();
        bw.writeInt64(millisec);
        return bw;
    }
}
