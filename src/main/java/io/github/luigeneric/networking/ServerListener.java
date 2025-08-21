package io.github.luigeneric.networking;


import io.github.luigeneric.core.AbstractConnection;
import io.github.luigeneric.core.IServerListener;
import io.github.luigeneric.core.IServerListenerSubscriber;

import java.io.IOException;
import java.net.SocketException;

public class ServerListener implements IServerListener
{
    private final ConnectionAccepter connectionAccepter;
    private IServerListenerSubscriber newConnectionSubscriber;
    private boolean isShutdown;

    public ServerListener(final int port, final int backlog)
    {
        try
        {
            this.connectionAccepter = new ConnectionAccepter(port, backlog);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        this.isShutdown = false;
    }
    @Override
    public void setServerListenerSubscriber(final IServerListenerSubscriber serverListenerSubscriber)
    {
        this.newConnectionSubscriber = serverListenerSubscriber;
    }

    @Override
    public void run()
    {
        while (!this.isShutdown)
        {
            try
            {
                final AbstractConnection newConnection = connectionAccepter.accept();
                if (this.newConnectionSubscriber != null)
                {
                    newConnectionSubscriber.notifyNewConnection(newConnection);
                }

            }
            catch (SocketException ignored)
            {}
            catch (final Exception e)
            {
                e.printStackTrace();
                this.shutdown();
            }
        }
    }

    @Override
    public boolean isShutdown()
    {
        return isShutdown;
    }


    @Override
    public synchronized void shutdown()
    {
        if (!isShutdown)
        {
            this.isShutdown = true;
            this.connectionAccepter.shutdown();
        }
    }

    @Override
    public String toString()
    {
        return "ServerListener{" +
                "connectionAccepter=" + connectionAccepter +
                '}';
    }
}
