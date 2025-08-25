package io.github.luigeneric.core.protocols.subscribe;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.player.*;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.StatsProtocolSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class SubscribeProtocol extends BgoProtocol implements StatsProtocolSubscriber
{
    private final UsersContainer usersContainer;
    private final SubscribeProtocolWriteOnly writer;

    public SubscribeProtocol(final ProtocolContext ctx, final UsersContainer usersContainer)
    {
        super(ProtocolID.Subscribe, ctx);
        this.usersContainer = usersContainer;
        this.writer = new SubscribeProtocolWriteOnly();
    }

    public SubscribeProtocolWriteOnly writer()
    {
        return writer;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue((short) msgType);
        final Player thisPlayer = user().getPlayer();
        if (clientMessage == null)
        {
            log.error("ClientMessage was null for SubscribeProtocol, userID:" + thisPlayer.getUserID());
            return;
        }

        switch (clientMessage)
        {
            case Info ->
            {
                final long playerID = br.readUint32();
                final long flags = br.readUint32();
                final Set<InfoType> types = testInfoTypes(flags);

                handleInfoRequest(playerID, types);
            }
            //SubscribeInfo
            case Subscribe ->
            {
                final long playerID = br.readUint32();
                final long flags = br.readUint32();
                final Set<InfoType> types = testInfoTypes(flags);
                final Optional<User> optUser = this.usersContainer.get(playerID);
                if (optUser.isEmpty())
                    return;
                final User userToSubscribe = optUser.get();
                for (InfoType type : types)
                {
                    switch (type)
                    {
                        case Name ->
                        {
                            log.error(user().getUserLog() + " Subscribe on something that you never should subscribe on " + type);
                        }
                        case Level ->
                        {
                            userToSubscribe.getPlayer().getSkillBook().addSubscriber(new LevelSubscriberUser(user(), writer));
                        }
                        case Medal ->
                        {
                            userToSubscribe.getPlayer().getPlayerMedals().addSubscriber(new MedalSubscriberUser(user(), writer));
                        }
                        case Wing ->
                        {
                            log.info(user().getPlayer().getPlayerLog() + " subscribe to guild!");
                            userToSubscribe.getPlayer().getPlayerGuild().addSubscriber(new GuildSubscriberUser(user(), writer));
                        }
                        case Title ->
                        {
                            //not implemented for now!
                        }
                        case Ships ->
                        {
                            userToSubscribe.getPlayer().getHangar().addSubscriber(new ShipsSubscriberUser(user(), writer));
                        }
                        case Location ->
                        {
                            userToSubscribe.getPlayer().getLocation().addSubscriber(new LocationSubscriberUser(user(), writer));
                        }

                        default -> log.error(user().getUserLog() + " SubscribeProtocol: unknown type to sub at!" + type);
                    }
                }
            }
            //Unsubscribe info
            case Unsubscribe ->
            {
                final long playerID = br.readUint32();
                final long flags = br.readUint32();
                final Set<InfoType> types = testInfoTypes(flags);

                final Optional<User> optUser = this.usersContainer.get(playerID);
                if (optUser.isEmpty())
                    return;
                final User userToUnSubscribe = optUser.get();
                for (InfoType type : types)
                {
                    switch (type)
                    {
                        case Level ->
                        {
                            userToUnSubscribe.getPlayer().getSkillBook().removeSubscriber(thisPlayer.getUserID());
                        }
                        case Medal ->
                        {
                            userToUnSubscribe.getPlayer().getPlayerMedals().removeSubscriber(thisPlayer.getUserID());
                        }
                        case Wing ->
                        {
                            userToUnSubscribe.getPlayer().getPlayerGuild().removeSubscriber(thisPlayer.getUserID());
                        }
                        case Ships ->
                        {
                            userToUnSubscribe.getPlayer().getHangar().removeSubscriber(thisPlayer.getUserID());
                        }
                        case Location ->
                        {
                            userToUnSubscribe.getPlayer().getLocation().removeSubscriber(thisPlayer.getUserID());
                        }
                        case Title ->
                        {
                            //not implemented for now
                        }
                        default ->
                        {
                            log.error(user().getUserLog() + "Unsubscribe to type " + type + " not implemented!");
                        }
                    }
                }
            }
            case SubscribeStats ->
            {
                final long playerID = br.readUint32();
                final Optional<User> optPlayer = usersContainer.get(playerID);
                if (optPlayer.isPresent())
                {
                    final HangarShip activeShip = optPlayer.get().getPlayer().getHangar().getActiveShip();
                    if (playerID == thisPlayer.getUserID())
                    {
                        final PlayerProtocol playerProtocol = this.user().getProtocol(ProtocolID.Player);
                        activeShip.getShipStats().addSubscriber(playerProtocol);
                    }
                    else
                    {
                        activeShip.getShipStats().addSubscriber(this);
                    }
                }
            }
            case UnsubscribeStats ->
            {
                final long playerID = br.readUint32();
                final Optional<User> foundPlayer = this.usersContainer.get(playerID);
                if (foundPlayer.isPresent())
                {
                    final HangarShip activeShip = foundPlayer.get().getPlayer().getHangar().getActiveShip();
                    if ((playerID == thisPlayer.getUserID()))
                    {
                        activeShip.getShipStats().removeSubscriber(this.user().getProtocol(ProtocolID.Player));
                    }
                    else
                        activeShip.getShipStats().removeSubscriber(this);
                }
                else
                {
                    log.warn(user().getUserLog() + "UnsubscibeStats on not present PlayerShip");
                }
            }
            default -> log.warn(user().getUserLog() + "SubscribeProtocol " + clientMessage + " not implemented");
        }
    }

    private void handleInfoRequest(long playerID, Set<InfoType> types)
    {
        final Optional<User> optInfoRequstUser = usersContainer.get(playerID);
        if (optInfoRequstUser.isEmpty())
            return;
        final User infoRequestUser = optInfoRequstUser.get();
        for (InfoType infoType : types)
        {
            switch (infoType)
            {
                case Name ->
                {
                    user().send(writer.writePlayerName(playerID, infoRequestUser.getPlayer().getName()));
                }
                case Faction ->
                {
                    user().send(writer.writePlayerFaction(playerID, infoRequestUser.getPlayer().getFaction()));
                }
                case Avatar ->
                {
                    user().send(writer.writePlayerAvatar(playerID, infoRequestUser.getPlayer().getAvatarDescription().get()));
                }
                case Ships ->
                {
                    final Hangar hangar = infoRequestUser.getPlayer().getHangar();
                    final String shipName = hangar.getActiveShip().getName();
                    final List<Long> guids = hangar.getSortedGUIDs();
                    HangarShipsUpdate hangarShipsUpdate = new HangarShipsUpdate(shipName, guids);
                    user().send(writer.writePlayerShips(playerID, hangarShipsUpdate));
                }
                case Level ->
                {
                    final short level = infoRequestUser.getPlayer().getSkillBook().get();
                    user().send(writer.writePlayerLevel(playerID, level));
                }
                case Medal ->
                {
                    final MedalStatus medals = infoRequestUser.getPlayer().getPlayerMedals().get();
                    user().send(writer.writePlayerMedal(playerID, medals));
                }
                case Location ->
                {
                    user().send(writer.writePlayerLocation(playerID, infoRequestUser.getPlayer().getLocation()));
                }
                case Wing ->
                {
                    final Optional<Guild> optGuild = infoRequestUser.getPlayer().getGuild();
                    if (optGuild.isPresent())
                    {
                        final Guild guild = optGuild.get();
                        try
                        {
                            user().send(writer.writePlayerGuild(playerID, guild));
                            user().send(writer.writePlayerLocation(playerID, infoRequestUser.getPlayer().getLocation()));
                        }
                        catch (IllegalArgumentException illegalArgumentException)
                        {
                            log.error(user().getUserLog() + "Weird stacktrace " + illegalArgumentException.getMessage());
                            illegalArgumentException.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public static Set<InfoType> testInfoTypes(final long requestSent)
    {
        final Set<InfoType> rv = new HashSet<>();
        for (final InfoType type : InfoType.values())
        {
            final int res = (int) (requestSent & Integer.toUnsignedLong(type.getValue()));
            if (res > 0)
            {
                final InfoType extracted = InfoType.forValue(res);
                if (extracted != null)
                    rv.add(extracted);
            }
        }
        return rv;
    }

    @Override
    public boolean sendSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer)
    {
        if (user() == null)
        {
            log.error("sendSpacePropertyBuffer but user was null");
            return false;
        }

        final BgoProtocolWriter bw = writer.writeSpacePropertyBuffer(spacePropertyBuffer);
        return this.user().send(bw);
    }

    @Override
    public long userId()
    {
        return userId;
    }


    enum ClientMessage
    {
        Info((short)1),
        Subscribe(((short)1) + 1),
        Unsubscribe(((short)1) + 2),
        SubscribeStats(((short)1) + 3),
        UnsubscribeStats(((short)1) + 4);

        public static final int SIZE = Short.SIZE;

        private final short shortValue;

        private static final class MappingsHolder
        {
            private static final Map<Short, ClientMessage> mappings = new HashMap<>();
        }

        private static Map<Short, ClientMessage> getMappings()
        {
            return MappingsHolder.mappings;
        }

        ClientMessage(short value)
        {
            shortValue = value;
            getMappings().put(value, this);
        }
        ClientMessage(int value)
        {
            this((short) value);
        }

        public short getValue()
        {
            return shortValue;
        }

        public static ClientMessage forValue(short value)
        {
            return getMappings().get(value);
        }
    }


    public enum ServerMessage
    {
        PlayerName(1),
        PlayerFaction(2),
        PlayerAvatar(3),
        PlayerShips(4),
        PlayerStatus(5),
        PlayerLocation(6),
        PlayerLevel(7),
        PlayerGuild(8),
        PlayerStats(9),
        PlayerTitle(10),
        PlayerMedal(11),
        PlayerLogout(12),
        @Deprecated
        PlayerTournamentIndicator(13);

        public static final int SIZE = Short.SIZE;

        public final short value;


        private static final class MappingsHolder
        {
            private static final Map<Short, ServerMessage> mappings = new HashMap<>();
        }

        private static Map<Short, ServerMessage> getMappings()
        {
            return MappingsHolder.mappings;
        }

        ServerMessage(short value)
        {
            this.value = value;
            getMappings().put(value, this);
        }
        ServerMessage(int i)
        {
            this((short) i);
        }

        public short getValue()
        {
            return value;
        }

        public static ServerMessage forValue(short value)
        {
            return getMappings().get(value);
        }
    }
}
