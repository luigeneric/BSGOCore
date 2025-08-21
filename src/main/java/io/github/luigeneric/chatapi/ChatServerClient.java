package io.github.luigeneric.chatapi;

import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


/**
 * Weird Chtserverclient api, would prefer http/REST based API :c
 */
@Slf4j
@ApplicationScoped
public class ChatServerClient implements ChatApi
{

    public static final long RECONNECT_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    private final Vertx vertx;
    private final GameServerParamsConfig gameServerParamsConfig;
    private NetClient netClient;
    private NetSocket netSocket;
    private String host;
    private int port;
    private boolean isConnected;
    private long reconnectAttempts;
    private long reconnectIntervalMillisecond;

    @Inject
    public ChatServerClient(Vertx vertx, GameServerParamsConfig gameServerParamsConfig)
    {
        this.vertx = vertx;
        this.gameServerParamsConfig = gameServerParamsConfig;
        this.reconnectAttempts = 0;
        this.reconnectIntervalMillisecond = RECONNECT_INTERVAL; // Start with this, will increase exponentially
    }

    @Override
    public void start()
    {
        this.host = gameServerParamsConfig.chatServerAddress();
        this.port = gameServerParamsConfig.chatServerPort();

        netClient = vertx.createNetClient(new NetClientOptions()
                .setReconnectAttempts(5)
                .setReconnectInterval(RECONNECT_INTERVAL));

        connect();
    }

    private void connect()
    {
        netClient.connect(port, host, result ->
        {
            if (result.succeeded())
            {
                log.info("Connected to chat server {}:{}", host, port);
                netSocket = result.result();
                isConnected = true;
                reconnectAttempts = 0;
                reconnectIntervalMillisecond = RECONNECT_INTERVAL;

                netSocket.closeHandler(v -> {
                    log.warn("Connection to chat server closed");
                    isConnected = false;
                    reconnect();
                });

            } else
            {
                log.error("Failed to connect to chat server: {}", result.cause().getMessage());
                isConnected = false;
                reconnect();
            }
        });
    }

    private void reconnect()
    {
        vertx.setTimer(reconnectIntervalMillisecond, id -> {
            log.info("Attempting to reconnect to chat server (attempt {})", reconnectAttempts + 1);
            connect();
        });
        reconnectAttempts++;
        reconnectIntervalMillisecond = Math.min(reconnectIntervalMillisecond * 2, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void sendUserPosition(final long userID, final long sectorID)
    {
        if (!isConnected || netSocket == null)
        {
            log.warn("Cannot send position update: Not connected to chat server");
            return;
        }

        final String message = "bs%admin@" + userID + "@" + sectorID + "#";
        netSocket.write(Buffer.buffer(message), ar -> {
            if (ar.succeeded())
            {
                log.debug("Position update sent successfully");
            } else
            {
                log.error("Failed to send position update: {}", ar.cause().getMessage());
            }
        });
    }

    /**
     * Not implemented since the chatserver can't (?)
     */
    @Override
    public void userJoinedPartyId(long playerId, long partyId)
    {

    }

    /**
     * Not implemented since the chatserver can't (?)
     */
    @Override
    public void userLeftParty(long playerId)
    {

    }

    /**
     * Not implemented since the chatserver can't (?)
     */
    @Override
    public void userJoinedGuild(long playerId, long guildId)
    {

    }

    /**
     * Not implemented since the chatserver can't (?)
     */
    @Override
    public void userLeftGuild(long playerId)
    {

    }

    @Override
    @PreDestroy
    public void stop()
    {
        if (netSocket != null)
        {
            netSocket.close();
        }
        if (netClient != null)
        {
            netClient.close();
        }
    }
}