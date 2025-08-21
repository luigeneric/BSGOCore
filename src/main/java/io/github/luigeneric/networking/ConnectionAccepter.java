package io.github.luigeneric.networking;

import io.github.luigeneric.core.AbstractConnection;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConnectionAccepter
{
    private final ServerSocket serverSocket;

    protected ConnectionAccepter(final int port, final int backlog) throws IOException
    {
        this.serverSocket = new ServerSocket(port, backlog);
        this.serverSocket.setReuseAddress(true);
    }


    public AbstractConnection accept() throws Exception
    {
        final Socket incomingClient = this.serverSocket.accept();
        final int timeout = (int) TimeUnit.SECONDS.toMillis(10);
        incomingClient.setSoTimeout(timeout);
        //return new Connection(incomingClient);
        log.info("Accept new connection {}", incomingClient.getRemoteSocketAddress());
        return new ConnectionProxy(incomingClient, 60_000);
    }
    public void shutdown()
    {
        try
        {
            this.serverSocket.close();
        }
        catch (IOException ignored) {
            log.warn("socket already closed", ignored);
        }
    }

    @Override
    public String toString()
    {
        return "ConnectionAccepter{" +
                "serverSocket=" + serverSocket +
                '}';
    }
}
