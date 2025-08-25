package io.github.luigeneric.core.protocols.universe;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.galaxy.Galaxy;
import io.github.luigeneric.core.galaxy.IGalaxySubscriber;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.enums.Faction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class UniverseProtocol extends BgoProtocol implements IGalaxySubscriber
{
    private long id;
    private Faction faction;

    public UniverseProtocol(final ProtocolContext ctx)
    {
        super(ProtocolID.Universe, ctx);
    }

    @Override
    public void injectUser(User user)
    {
        super.injectUser(user);
        this.faction = user().getPlayer().getFaction();
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue(msgType);
        if (clientMessage == null)
            return;

        this.id = user().getPlayer().getUserID();

        switch (clientMessage)
        {
            //oberse galaxymap
            case SubscribeGalaxyMap ->
            {
                ctx.galaxy().addSubscriber(this);
            }
            //remove from observables
            case UnsubscribeGalaxyMap ->
            {
                ctx.galaxy().removeSubscriber(this);
            }
        }
    }

    @Override
    public void onDisconnect()
    {
        super.onDisconnect();
        ctx.galaxy().removeSubscriber(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniverseProtocol that = (UniverseProtocol) o;

        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public long getID()
    {
        return this.id;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public void mapUpdateReceived(final BgoProtocolWriter galaxyMapUpdatesProtocolWriter)
    {
        if (user() == null || !user().isConnected())
        {
            log.info("User subscribed to universe but user is null or disconnected, remove from subscribers");
            ctx.galaxy().removeSubscriber(this);
            return;
        }

        this.user().send(galaxyMapUpdatesProtocolWriter);
    }


}
