package io.github.luigeneric.core;

import io.github.luigeneric.Configuration;
import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.ScheduledService;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.community.guild.GuildRegistry;
import io.github.luigeneric.core.community.party.PartyRegistry;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.galaxy.Galaxy;
import io.github.luigeneric.core.gameplayalgorithms.ExperienceToLevelAlgo;
import io.github.luigeneric.core.player.login.SessionRegistry;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.debug.RefundProcessor;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.core.sector.management.SectorRegistry;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class GameServer implements IServerListenerSubscriber, UserDisconnectedSubscriber
{
    private final GameServerParamsConfig gameServerParams;
    private final Galaxy galaxy;
    private final ExecutorService executorService;
    private final ScheduledService scheduledService;
    private final DbProvider dbProviderProvider;
    private final SessionRegistry sessionRegistry;
    private final PartyRegistry partyRegistry;
    private final GuildRegistry guildRegistry;
    private final IServerListener serverListener;
    private final SectorRegistry sectorRegistry;
    private final UsersContainer usersContainer;
    private final ExperienceToLevelAlgo experienceToLevelAlgo;
    private final MicrometerRegistry micrometerRegistry;
    private final ChatAccessBlocker chatAccessBlocker;
    private final MissionUpdater missionUpdater;
    private final Catalogue catalogue;


    public GameServer(final GameServerParamsConfig gameServerParams,
                      final Galaxy galaxy,
                      final DbProvider dbProviderProvider,
                      final SessionRegistry sessionRegistry,
                      final PartyRegistry partyRegistry,
                      final GuildRegistry guildRegistry,
                      final IServerListener serverListener,
                      final UsersContainer usersContainer,
                      final SectorRegistry sectorRegistry,
                      final ExperienceToLevelAlgo experienceToLevelAlgo,
                      final ScheduledService scheduledService,
                      final MicrometerRegistry micrometerRegistry,
                      final ChatAccessBlocker chatAccessBlocker,
                      final MissionUpdater missionUpdater,
                      final Catalogue catalogue
    )
    {
        this.scheduledService = scheduledService;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.gameServerParams = gameServerParams;
        this.galaxy = galaxy;
        this.dbProviderProvider = dbProviderProvider;
        this.sessionRegistry = sessionRegistry;
        this.partyRegistry = partyRegistry;
        this.guildRegistry = guildRegistry;
        this.serverListener = serverListener;
        this.sectorRegistry = sectorRegistry;
        this.serverListener.setServerListenerSubscriber(this);
        this.usersContainer = usersContainer;
        this.experienceToLevelAlgo = experienceToLevelAlgo;
        this.micrometerRegistry = micrometerRegistry;
        this.chatAccessBlocker = chatAccessBlocker;
        this.missionUpdater = missionUpdater;
        this.catalogue = catalogue;
    }


    @Override
    public void notifyNewConnection(final AbstractConnection newConnection)
    {
        final Configuration configuration = CDI.current().select(Configuration.class).get();
        final ProtocolRegistry protocolRegistry = new ProtocolRegistry(this.gameServerParams, this.galaxy, this.sectorRegistry,
                this.dbProviderProvider, this.sessionRegistry, this.partyRegistry, this.guildRegistry,
                this.usersContainer, newConnection, this.experienceToLevelAlgo,
                this, scheduledService, micrometerRegistry, configuration.characterServices(), chatAccessBlocker, missionUpdater,
                new RefundProcessor(catalogue));
        final ProtocolUpdater protocolUpdater = new ProtocolUpdater(newConnection, protocolRegistry);
        this.executorService.execute(protocolUpdater);
    }

    public void start()
    {
        Thread.ofVirtual()
                .name("GameServer-ServerListener")
                .start(serverListener);
    }

    public void shutdownProcess()
    {
        log.warn("ServerListener stopping...");
        serverListener.shutdown();
        log.warn("ServerListener stopped");

        log.warn("Disconnecting all online users...");
        final Set<User> allOnlineUsers = this.usersContainer.userSet(User::isConnected);
        for (final User user : allOnlineUsers)
        {
            try
            {
                final SceneProtocol sceneProtocol = user.getProtocol(ProtocolID.Scene);
                final BgoProtocolWriter disconnectBw = sceneProtocol.writeDisconnect();
                user.send(disconnectBw);
            }
            catch (NoSuchElementException noSuchElementException)
            {
                noSuchElementException.printStackTrace();
            }
        }

        final Set<User> stillOnline = this.usersContainer.userSet(User::isConnected);
        for (final User onlineUser : stillOnline)
        {
            onlineUser.getConnection().ifPresent(connection -> connection.closeConnection("Shutdown process, user was still online"));
        }
        log.warn("All onlineusers disconnected");

        log.warn("SectorRegistry stopping...");
        this.sectorRegistry.shutdown();
        log.warn("SectorRegistry stopped");

        log.info("Writing guilds....");
        this.dbProviderProvider.writeGuilds(guildRegistry);
        log.info("Writing guilds finished");

        log.info("shutting down now");
        this.executorService.shutdownNow();
        boolean terminated = false;
        try
        {
            terminated = this.executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
            log.info("shutdown error", e);
        }
        log.info("shutdown finished {}", terminated);
    }


    @Override
    public void onDisconnect(final User user)
    {
        final Optional<User> optUser = this.usersContainer.get(user.getPlayer().getUserID());
        if (optUser.isPresent())
        {
            user.getProtocolRegistry().onDisconnect();
            this.dbProviderProvider.writePlayerToDb(user.getPlayer());
        }
    }
}
