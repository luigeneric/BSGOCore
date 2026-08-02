package io.github.luigeneric.core.protocols.scene;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.chatapi.ChatApi;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.community.CommunityProtocol;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;



@Slf4j
public class SceneProtocol extends BgoProtocol
{
    private ScheduledFuture<?> logoutFuture;
    private boolean properlyLoggedOut;
    private final AtomicBoolean sceneLoadedFlag;
    private final ChatApi chatApi;

    public SceneProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Scene, ctx);
        this.properlyLoggedOut = true;
        this.sceneLoadedFlag = new AtomicBoolean(false);
        this.chatApi = CDI.current().select(ChatApi.class).get();
    }

    @Override
    public void injectUser(final User user)
    {
        this.logoutFuture = null;
        this.properlyLoggedOut = false;

        super.injectUser(user);
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br)
    {
        final ClientMessage clientMessage = ClientMessage.valueOf(msgType);
        if (clientMessage == null)
            return;

        switch (clientMessage)
        {
            case SceneLoaded ->
            {
                this.sceneLoadedFlag.set(true);
            }
            case QuitLogin ->
            {
                final Player player = user().getPlayer();
                final Location location = player.getLocation();
                location.setLocation(location.getNonDisconnectLocation(), location.getSectorID(), location.getSectorGUID());
                sendLoadNextScene();
            }
            case StopDisconnect ->
            {
                cancelLogout();
            }
            case Disconnect ->
            {
                switch (user().getPlayer().getLocation().getGameLocation())
                {
                    case Room ->
                    {
                        scheduleLogout(15);
                    }
                    case Space ->
                    {
                        final boolean isInCombat = user().getPlayer().getHangar().getActiveShip().getShipStats().isInCombat();
                        final long delay = isInCombat ? combatLogoutDelay() : 15;
                        scheduleLogout(delay);
                    }
                }


            }
            default -> log.error("Unknown messageType in SceneProtocol: " + msgType);
        }
    }

    private void scheduleLogout(final long delaySeconds)
    {
        if (logoutFuture != null)
            return;

        user().send(writeDisconnectTimer(delaySeconds));
        final Runnable r = () ->
        {
            user().send(writeDisconnect());
            logoutFuture = null;
        };
        this.logoutFuture = ctx.scheduledExecutorService().schedule(r, delaySeconds, TimeUnit.SECONDS);
    }
    private void cancelLogout()
    {
        if (this.logoutFuture == null || this.logoutFuture.isCancelled())
            return;

        this.logoutFuture.cancel(false);
        this.logoutFuture = null;
    }
    public boolean isLoggingOut()
    {
        return this.logoutFuture != null;
    }
    public boolean isLogoutFinished()
    {
        return this.properlyLoggedOut && this.logoutFuture != null;
    }


    private long combatLogoutDelay()
    {
        final byte tier = user().getPlayer().getHangar().getActiveShip().getShipCard().getTier();
        return 45 + tier * 15;
    }

    public BgoProtocolWriter writeDisconnect()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Disconnect.value);
        return bw;
    }

    public BgoProtocolWriter writeDisconnectTimer(final float disconnectTimer)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.DisconnectTimer.value);
        bw.writeSingle(disconnectTimer);
        return bw;
    }

    public void sendLoadNextScene()
    {
        final Location location = user().getPlayer().getLocation();

        // DisconnectLocation.process() writes ZERO bytes, so a LoadNextScene emitted while the
        // player is in that state has an empty body: the client reads past the end of the
        // message, throws EndOfStreamException, swallows it, and never loads a scene - an
        // infinite "Loading... please wait" with a completely clean server log.
        // This is reachable on any re-login inside the 15-minute InactiveKicker window, because
        // the cached User is still in the Disconnect state installed on socket close. Substitute
        // the last real location instead. Covers every caller of this method, not just login.
        if (location.getGameLocation() == GameLocation.Disconnect)
        {
            location.setLocation(location.getNonDisconnectLocation(),
                    location.getSectorID(), location.getSectorGUID());
        }

        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.LoadNextScene.value);
        bw.writeDesc(location);

        this.sceneLoadedFlag.set(false);

        final CommunityProtocol communityProtocol = user().getProtocol(ProtocolID.Community);
        if (communityProtocol.isChatConnected())
            chatApi.sendUserPosition(user().getPlayer().getUserID(), location.getSectorID());

        user().send(bw);
    }


    public enum DisconnectState
    {
        NotScheduled,
        StartTimer,
        TimerRunning,
        StartDisconnectRequest,
        AbortDisconnect
    }


}
