package io.github.luigeneric.core.protocols.login;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.*;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.factors.Factor;
import io.github.luigeneric.core.player.login.Session;
import io.github.luigeneric.core.player.login.SessionRegistry;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.IProtocolRegistry;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.community.CommunityProtocol;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.core.protocols.setting.SettingProtocol;
import io.github.luigeneric.enums.FactorSource;
import io.github.luigeneric.enums.FactorType;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class LoginProtocol extends BgoProtocol
{
    private final SessionRegistry sessionRegistry;
    private final DbProvider dbProviderProvider;
    private final AbstractConnection connection;
    private final UsersContainer usersContainer;
    private final IProtocolRegistry protocolRegistry;
    private final UserDisconnectedSubscriber userDisconnectedSubscriber;
    private final GameServerParamsConfig gameServerParams;
    private final LoginProtocolWriteOnly writer;
    private final MissionUpdater missionUpdater;
    public LoginProtocol(final SessionRegistry sessionRegistry, final DbProvider dbProviderProvider, final AbstractConnection connection,
                         final UsersContainer usersContainer, final IProtocolRegistry protocolRegistry,
                         final UserDisconnectedSubscriber userDisconnectedSubscriber, final GameServerParamsConfig gameServerParams,
                         final MissionUpdater missionUpdater
    )
    {
        super(ProtocolID.Login);
        this.sessionRegistry = sessionRegistry;
        this.dbProviderProvider = dbProviderProvider;
        this.connection = connection;
        this.usersContainer = usersContainer;
        this.protocolRegistry = protocolRegistry;
        this.userDisconnectedSubscriber = userDisconnectedSubscriber;
        this.gameServerParams = gameServerParams;
        this.missionUpdater = missionUpdater;
        this.writer = new LoginProtocolWriteOnly();
    }

    public LoginProtocolWriteOnly writer()
    {
        return this.writer;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final LoginProtocolClientMessage loginProtocolClientMessage = LoginProtocolClientMessage.valueOf(msgType);
        switch (loginProtocolClientMessage)
        {
            case Init ->
            {
                connection.send(writer.writeSrvRevision());
            }
            case Player ->
            {
                final byte connectionTypeArg = (byte) br.read();
                final long playerIDArg = br.readUint32(); //useless value
                final String playerNameArg = br.readString(); //useless value

                final String sessionCode = br.readString(); //only good value

                boolean isModifiedAssemblyFlag = false;

                //SESSION HANDLING
                //-1 if session not existing
                final SessionHandlingResult sessionHandlingResult = sessionHandling(sessionCode, connection);
                if (!sessionHandlingResult.isValid())
                {
                    log.info("Session is not valid, stop Login");
                    return;
                }

                final long playerID = sessionHandlingResult.playerID();
                MDC.put("userID", String.valueOf(playerID));
                log.info("User tries to login with session {}", sessionHandlingResult.session());
                log.info("Assembly MD5 {}", playerNameArg);
                if (!gameServerParams.ignoreHashes() && !gameServerParams.allowedHashes().contains(playerNameArg))
                {
                    log.warn("Cheat, player used modified assembly! {} {}", playerIDArg, playerID);
                    isModifiedAssemblyFlag = true;
                }
                final Optional<Session> optSession = sessionRegistry.getSession(sessionCode);
                if (optSession.isEmpty())
                {
                    log.info("User session is not present for registry {}", sessionCode);
                    return;
                }
                final Session session = optSession.get();
                PlayerlessUser playerlessUser = new PlayerlessUser(connection, session, protocolRegistry);
                this.usersContainer.addWithoutChar(playerID, playerlessUser);

                final Optional<User> optExUser = this.getExistingUser(playerID);

                //no user present, create new account!
                if (optExUser.isEmpty())
                {
                    log.info("No existing account found for player {}", playerID);
                    var missionBook = new MissionBook(playerID, missionUpdater);
                    final Player player = new Player(playerID, gameServerParams, missionBook);
                    //TODO add try-catch clause to check if there is no pre-char
                    final User newUser = this.usersContainer.playerCreated(player);
                    player.setModifiedAssemblyFlag(isModifiedAssemblyFlag);

                    //connection notifies the old session to close
                    // therefore we need to set the connection first and afterwards set the session
                    newUser.setConnection(connection);
                    newUser.setSession(session);
                    newUser.setUserDisconnectedSubscriber(this.userDisconnectedSubscriber);
                    connection.send(writer.writePlayer(newUser.getPlayer().getBgoAdminRoles()));
                    //setup registry so all protocols work
                    newUser.getProtocolRegistry().loginFinished(newUser);

                    final LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

                    if (!newUser.getPlayer().getFactors().hasFactorSource(FactorSource.Holiday)
                    && LocalDateTime.now(Clock.systemUTC()).isBefore(endTime))
                    {
                        newUser.getPlayer().getFactors().addFactor(Factor.fromEndTime(FactorType.Loot, FactorSource.Holiday, 1f, endTime));
                        newUser.getPlayer().getFactors().addFactor(Factor.fromEndTime(FactorType.Experience, FactorSource.Holiday, 1f, endTime));
                        newUser.getPlayer().getFactors().addFactor(Factor.fromEndTime(FactorType.AsteroidYield, FactorSource.Holiday, 1f, endTime));
                        newUser.send(new PlayerProtocolWriteOnly().writeFactors(newUser.getPlayer().getFactors()));
                    }

                    final CommunityProtocol communityProtocol = newUser.getProtocolRegistry().getProtocol(ProtocolID.Community);
                    communityProtocol.sendChatSessionId("", 860, "us", gameServerParams.chatServerAddress());
                }
                //there is already an existing account
                else
                {
                    final boolean containedRemoveChar = usersContainer.removeWithoutChar(playerID) != null;
                    log.info("contained remove char on login: " + containedRemoveChar);
                    final User existingUser = optExUser.get();
                    this.injectUser(existingUser);
                    existingUser.getPlayer().setModifiedAssemblyFlag(isModifiedAssemblyFlag);

                    if (user.getProtocolRegistry().getAllProtocols().size() == 1)
                    {
                        this.protocolRegistry.loginFinished(user);
                    }
                    else
                    {
                        this.protocolRegistry.injectOldRegistry(user);
                    }

                    final SettingProtocol settingProtocol = this.protocolRegistry.getProtocol(ProtocolID.Setting);
                    settingProtocol.sendSettings();
                    user.setConnection(connection);
                    user.setSession(session);
                    user.send(writer.writePlayer(user.getPlayer().getBgoAdminRoles()));
                    PlayerProtocol playerProtocol = user.getProtocol(ProtocolID.Player);
                    user.setUserDisconnectedSubscriber(this.userDisconnectedSubscriber);
                    protocolRegistry.injectOldRegistry(user);
                    playerProtocol.sendCharacter();
                    /*
                    PrometheusMetrics.INSTANCE.getPlayersOnline()
                            .labels(user.getPlayer().getFaction().name())
                            .inc();
                     */
                    final CommunityProtocol communityProtocol = this.protocolRegistry.getProtocol(ProtocolID.Community);
                    communityProtocol.sendChatSessionId("", 860, "us", gameServerParams.chatServerAddress());
                }
            }
            case Echo ->
            {
                //should never happen
            }
            default ->
            {
                log.error("Unknown messageType in LoginProtocol.");
            }
        }
    }

    /**
     * Handles all the session stuff
     * @param sessionCode The Session-Code received from the client
     * @param connection the client-object itself
     * @return the actual userID, positive if valid, -1 if error
     */
    private SessionHandlingResult sessionHandling(final String sessionCode, final AbstractConnection connection)
    {
        final Optional<Session> optSession = sessionRegistry.getSession(sessionCode);
        if (optSession.isEmpty())
        {
            connection.send(writer.writeLoginError(LoginError.WrongSession, "Could not find session"));
            log.warn("Login failed for session, could not find: " + sessionCode);
            return SessionHandlingResult.invalidSession();
        }
        final Session session = optSession.get();

        final boolean alreadyLoggedIn = sessionRegistry.checkUserAlreadyLoggedIn(session);
        if (alreadyLoggedIn)
        {
            log.warn("USER ALREADY LOGGED IN " + session.getUserId());
            //final Set<Session> sessionsOnUser = sessionRegistry.getSessions(session.getUserId());

            final Optional<User> optCurrentOnlineUser = usersContainer.get(session.getUserId());
            if (optCurrentOnlineUser.isPresent())
            {
                final User alreadyLoggedInUser = optCurrentOnlineUser.get();

                final Optional<AbstractConnection> optConnection = alreadyLoggedInUser.getConnection();
                //invalidate current session in use
                if (optConnection.isEmpty() || optConnection.get().isClosed())
                {
                    log.error("..But user is not online anymore... fix session state!");
                    session.useSession();
                    return SessionHandlingResult.validSession(session.getUserId(), sessionCode);
                }
            }

            connection.send(writer.writeLoginError(LoginError.AlreadyConnected, "User already in use"));
            return SessionHandlingResult.invalidSession(session.getUserId());
        }
        session.useSession();
        return SessionHandlingResult.validSession(session.getUserId(), sessionCode);
    }

    private Optional<User> getExistingUser(final long userID)
    {
        final Optional<User> optExUser = this.usersContainer.get(userID);
        if (optExUser.isPresent())
            return optExUser;

        //if not, there are two possibilities: either there is no User yet or there is first user inside the database!

        //database handling
        final boolean userExists = this.dbProviderProvider.checkIfUserExists(userID);
        //no user yet? return empty
        if (!userExists)
            return Optional.empty();

        final Player fetchedPlayer = this.dbProviderProvider.fetchPlayer(userID);
        return Optional.ofNullable(this.usersContainer.playerCreated(fetchedPlayer));
    }
}