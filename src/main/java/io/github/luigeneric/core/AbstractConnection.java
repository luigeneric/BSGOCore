package io.github.luigeneric.core;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
public abstract class AbstractConnection implements IConnection
{
    protected ConnectionClosedSubscriber connectionClosedSubscriber;
    protected final LocalDateTime connectionTimeStamp;
    protected final Socket socket;

    public AbstractConnection(final Socket socket)
    {
        this.socket = socket;
        this.connectionTimeStamp = LocalDateTime.now(Clock.systemUTC());
    }

    /**
     * Send first given ProtocolWriter(first buffer) to the connection.
     * @param bw the ProtocolWriter
     * @return true if the send call was successfully, false if not
     */
    public abstract boolean send(final BgoProtocolWriter bw);
    public abstract boolean send(final Collection<BgoProtocolWriter> bws);


    public void setConnectionClosedSubscriber(final ConnectionClosedSubscriber connectionClosedSubscriber)
    {
        if (connectionClosedSubscriber != null)
            this.connectionClosedSubscriber = connectionClosedSubscriber;
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return socket.getRemoteSocketAddress();
    }

    public LocalDateTime getConnectionTimeStamp()
    {
        return connectionTimeStamp;
    }

    public String addressPrettyPrint()
    {
        if (getRemoteSocketAddress() == null)
            return "";
        try
        {
            return ((InetSocketAddress)this.getRemoteSocketAddress()).getAddress().getHostAddress() + " ";
        }
        catch (Exception ex)
        {
            log.error("Fix Address pretty print " + ex.getMessage());
        }
        return "error";
    }
    public String getIpRemoteHostAddress()
    {
        if (getRemoteSocketAddress() == null)
            return "";
        return ((InetSocketAddress)this.getRemoteSocketAddress()).getAddress().getHostAddress();
    }

    public void setSoTimeout(final int waitMs) throws SocketException
    {
        this.socket.setSoTimeout(waitMs);
    }
}
