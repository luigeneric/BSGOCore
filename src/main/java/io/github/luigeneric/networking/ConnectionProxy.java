package io.github.luigeneric.networking;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ConnectionProxy extends Connection
{
    private final long startTime;
    private double totalIncoming;
    private double maxBytesPerSecond;
    private final double bytesPerSecondLimitBeforeKick;
    public ConnectionProxy(final Socket socket, final double bytesPerSecondLimitBeforeKick) throws IOException
    {
        super(socket);
        this.bytesPerSecondLimitBeforeKick = bytesPerSecondLimitBeforeKick;
        this.startTime = System.currentTimeMillis();
        this.totalIncoming = 0;
        this.maxBytesPerSecond = 0;
    }

    @Override
    public BgoProtocolReader recvNextMessage() throws IllegalStateException
    {
        final BgoProtocolReader tmp = super.recvNextMessage();

        this.totalIncoming += tmp.getSize();

        final long elapsed = System.currentTimeMillis() - startTime;
        final double seconds = elapsed * 0.001;
        final double bytesPerSecond = totalIncoming / seconds;
        this.maxBytesPerSecond = Math.max(maxBytesPerSecond, bytesPerSecond);

        if (bytesPerSecond >= bytesPerSecondLimitBeforeKick)
        {
            String issueText = "Critical! kill connection because too many bytes send by client! " + getSocket().getRemoteSocketAddress().toString() + " "
                    + bytesPerSecond  + "/" + bytesPerSecondLimitBeforeKick;
            this.closeConnection("Connection closed because " + issueText);
        }

        return tmp;
    }
}
