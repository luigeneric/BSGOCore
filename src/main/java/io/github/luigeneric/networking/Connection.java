package io.github.luigeneric.networking;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.AbstractConnection;
import io.github.luigeneric.networking.sender.BlockingQueueSender;
import io.github.luigeneric.networking.sender.IPacketSender;
import io.github.luigeneric.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collection;

@Slf4j
public class Connection extends AbstractConnection
{
    private final InputStream input;
    private boolean isClosed;
    private final IPacketSender packetSender;

    public Connection(final Socket socket) throws IOException
    {
        super(socket);
        this.socket.setTcpNoDelay(true);
        this.input = new BufferedInputStream(socket.getInputStream(), 65535);
        OutputStream output = new BufferedOutputStream(socket.getOutputStream(), 65535);
        this.isClosed = false;
        this.packetSender = new BlockingQueueSender(output);
    }



    public boolean send(final BgoProtocolWriter bw)
    {
        return packetSender.send(bw);
    }

    @Override
    public boolean send(final Collection<BgoProtocolWriter> bws)
    {
        return packetSender.send(bws);
    }

    Socket getSocket()
    {
        return this.socket;
    }


    public BgoProtocolReader recvNextMessage() throws IllegalStateException
    {
        if (this.isClosed)
            throw new IllegalStateException("Connection is closed");

        try
        {
            if (!this.socket.isClosed())
            {
                final byte[] lenBytes = this.readByteArrayLen(2);
                final int toReceivePacketLen = BgoProtocolReader.readBufferSize(lenBytes);
                final byte[] actualPacket = this.readByteArrayLen(toReceivePacketLen);
                return new BgoProtocolReader(actualPacket);
            }
        }
        catch (final SocketTimeoutException socketTimeoutException)
        {
            //log.warn("Socket timeout exception! {} stack={}",
            //        getRemoteSocketAddress(), Utils.getExceptionStackTrace(socketTimeoutException));
            this.closeConnection(String.format("Socket timeout exception %s", getRemoteSocketAddress()));
        }
        catch (final IOException ioException)
        {
            this.closeConnection("IOException upon read! " + Utils.getExceptionStackTrace(ioException));
        }
        throw new IllegalStateException("RecvCall: Socket is already closed");
    }

    private byte[] readByteArrayLen(final int len) throws IOException
    {
        final byte[] arr = new byte[len];
        int alreadyRead = 0;
        int nowRead = 0;
        while (alreadyRead < len && nowRead != -1)
        {
            nowRead = this.input.read(arr, alreadyRead, len - alreadyRead);
            alreadyRead += nowRead;
        }
        //end of the stream is reached
        if (nowRead == -1)
        {
            throw new IOException("The end of the stream is reached");
        }
        return arr;
    }

    @Override
    public boolean isClosed()
    {
        return this.isClosed;
    }

    @Override
    public void closeConnection(final String reason)
    {
        try
        {
            if (!socket.isClosed())
                this.socket.close();
        } catch (IOException ioException)
        {
            log.warn("IOException upon close connection", ioException);
        }
        finally
        {
            if (!isClosed)
            {
                this.isClosed = true;
                this.notifyConnectionClosed(reason);
            }
            this.packetSender.shutdown();
        }
    }

    private void notifyConnectionClosed(final String reason)
    {
        if (this.connectionClosedSubscriber == null)
            return;

        this.connectionClosedSubscriber.onConnectionClosed(this, reason);
    }
}
