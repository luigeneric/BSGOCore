package io.github.luigeneric.core;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.location.DisconnectLocation;
import io.github.luigeneric.core.player.login.Session;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.IProtocolRegistry;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class User implements ConnectionClosedSubscriber
{
    private AbstractConnection connection;
    private Session session;
    private final Player player;
    private final IProtocolRegistry protocolRegistry;
    private UserDisconnectedSubscriber userDisconnectedSubscriber;
    private SocketAddress socketAddress;
    private LocalDateTime lastAccountSafe;
    private final Lock lock;

    public User(final AbstractConnection connection, final Player player, final IProtocolRegistry protocolRegistry,
                final Session session)
    {
        this.lock = new ReentrantLock();
        this.connection = Objects.requireNonNull(connection);
        this.player = Objects.requireNonNull(player);
        MDC.put("userID", String.valueOf(player.getUserID()));
        this.protocolRegistry = Objects.requireNonNull(protocolRegistry);
        this.session = Objects.requireNonNull(session);
    }

    public static User fromPlayerless(final PlayerlessUser playerlessUser, final Player player)
    {
        return new User(playerlessUser.getConnection(), player, playerlessUser.getProtocolRegistry(), playerlessUser.getSession());
    }

    public void setSession(final Session session)
    {
        lock.lock();
        try
        {
            this.session.notifyClosed();
            this.session = session;
        }
        finally
        {
            lock.unlock();
        }
    }
    public Session getSession()
    {
        lock.lock();
        try
        {
            return this.session;
        }
        finally
        {
            lock.unlock();
        }
    }

    public String getUserLog()
    {
        return this.player.getPlayerLog() + " "  + (this.connection == null ? "" : this.connection.addressPrettyPrint());
    }
    public String getUserLogSimple()
    {
        return this.player.getPlayerLog();
    }

    public IProtocolRegistry getProtocolRegistry()
    {
        return protocolRegistry;
    }

    public Player getPlayer()
    {
        return this.player;
    }

    public <T extends BgoProtocol> T getProtocol(ProtocolID protocolID) throws NoSuchElementException
    {
        return this.protocolRegistry.getProtocol(protocolID);
    }

    public boolean send(final BgoProtocolWriter bw)
    {
        if (this.connection == null)
            return false;
        final boolean sendSuccessful = this.connection.send(bw);
        if (!sendSuccessful)
        {
            this.setConnection(null);
        }
        return sendSuccessful;
    }

    public boolean send(final Collection<BgoProtocolWriter> bws)
    {
        if (this.connection == null)
            return false;
        final boolean sendSuccessful = this.connection.send(bws);
        if (!sendSuccessful)
        {
            this.setConnection(null);
        }
        return sendSuccessful;
    }

    public void setConnection(final AbstractConnection connection)
    {
        lock.lock();
        try
        {
            if (this.getConnection().isPresent())
            {
                if (this.connection != connection)
                {
                    this.session.notifyClosed();
                    if (!this.connection.isClosed())
                        this.connection.closeConnection("Set new connection, close and replace old connection");
                }
            }
            else
            {
                if (this.session != null && this.session.getSessionState() == Session.SessionState.InUse)
                {
                    this.session.notifyClosed();
                }
            }
            this.connection = connection;
            if (this.connection != null)
            {
                this.socketAddress = this.connection.getRemoteSocketAddress();
                this.player.setLastRemoteAddress(this.socketAddress, false);
                this.connection.setConnectionClosedSubscriber(this);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public Optional<AbstractConnection> getConnection()
    {
        lock.lock();
        try
        {
            return Optional.ofNullable(this.connection);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void onConnectionClosed(final AbstractConnection connection, final String reason)
    {
        log.info("User={} disconnected", getUserLogSimple());
        this.setConnection(null);

        /*
        PrometheusMetrics.INSTANCE.getPlayersOnline()
                .labels(player.getFaction().name())
                .dec();
         */
        this.player.setLastLogout();
        this.player.setLastRemoteAddress(this.socketAddress, true);
        final boolean isInSpace = this.player.getLocation().getGameLocation() == GameLocation.Space;
        final GameProtocol gameProtocol = getProtocol(ProtocolID.Game);
        final boolean isRespawnSend = gameProtocol.isRespawnSend();
        if (isInSpace && isRespawnSend)
            gameProtocol.dockProcedure(true);
        this.player.getLocation().changeState(new DisconnectLocation(player.getLocation(), TransSceneType.None));
        if (this.userDisconnectedSubscriber != null)
            this.userDisconnectedSubscriber.onDisconnect(this);
    }

    public boolean isConnected()
    {
        return this.connection != null && !this.connection.isClosed();
    }

    public void setUserDisconnectedSubscriber(final UserDisconnectedSubscriber userDisconnectedSubscriber)
    {
        this.userDisconnectedSubscriber = userDisconnectedSubscriber;
    }

    public boolean isSameIp(final User other)
    {
        final SocketAddress remoteSocketAddr = getPlayer().getLastRemoteAddress();
        final String currentIpAddr = ((InetSocketAddress)remoteSocketAddr).getAddress().getHostAddress();

        final SocketAddress otherRemoteSocketAddr = other.getPlayer().getLastRemoteAddress();
        final String otherIpAddr = ((InetSocketAddress)otherRemoteSocketAddr).getAddress().getHostAddress();
        //log.info("currIp={} otherIp={}", currentIpAddr, otherIpAddr);

        return currentIpAddr.equals(otherIpAddr);
    }



    public Optional<LocalDateTime> getLastAccountSafe()
    {
        return Optional.ofNullable(lastAccountSafe);
    }

    public void setLastAccountSafe(final LocalDateTime lastAccountSafe)
    {
        this.lastAccountSafe = lastAccountSafe;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return Objects.equals(player, user.player);
    }

    @Override
    public int hashCode()
    {
        return player.hashCode();
    }
}
