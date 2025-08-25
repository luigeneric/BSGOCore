package io.github.luigeneric.core.protocols;


import io.github.luigeneric.ScheduledService;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public abstract class BgoProtocol extends WriteOnlyProtocol
{
    protected final ProtocolContext ctx;
    protected long userId;

    public BgoProtocol(final ProtocolID protocolID, final ProtocolContext ctx)
    {
        super(protocolID);
        this.userId = -1;
        this.ctx = ctx;
    }

    public User user()
    {
        return ctx.user();
    }

    public void injectUser(final User user)
    {
        this.ctx.user(user);
        if (user != null)
        {
            if (userId == -1)
            {
                this.userId = user.getPlayer().getUserID();
            }
            MDC.put("userID", String.valueOf(user.getPlayer().getUserID()));
        }
        setupHandlers();
    }

    protected void setupHandlers(){}

    public abstract void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException;


    public void onDisconnect()
    {
        //this.user = null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BgoProtocol that = (BgoProtocol) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), userId);
    }
}
