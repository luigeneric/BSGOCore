package io.github.luigeneric.core;


import io.github.luigeneric.core.player.login.Session;
import io.github.luigeneric.core.protocols.IProtocolRegistry;

public class PlayerlessUser
{
    private AbstractConnection connection;
    private Session session;
    private final IProtocolRegistry protocolRegistry;

    public PlayerlessUser(final AbstractConnection connection, final Session session, final IProtocolRegistry protocolRegistry)
    {
        this.connection = connection;
        this.session = session;
        this.protocolRegistry = protocolRegistry;
    }

    public AbstractConnection getConnection()
    {
        return connection;
    }

    public Session getSession()
    {
        return session;
    }

    public IProtocolRegistry getProtocolRegistry()
    {
        return protocolRegistry;
    }
}
