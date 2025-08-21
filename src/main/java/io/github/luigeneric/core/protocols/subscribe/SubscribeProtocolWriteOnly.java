package io.github.luigeneric.core.protocols.subscribe;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.community.guild.GuildMemberInfo;
import io.github.luigeneric.core.player.AvatarDescription;
import io.github.luigeneric.core.player.HangarShipsUpdate;
import io.github.luigeneric.core.player.MedalStatus;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SubscribeProtocolWriteOnly extends WriteOnlyProtocol
{
    public SubscribeProtocolWriteOnly()
    {
        super(ProtocolID.Subscribe);
    }


    public BgoProtocolWriter writeSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerStats.value);
        bw.writeUInt32(spacePropertyBuffer.getOwner().ownerID());
        bw.writeDesc(spacePropertyBuffer);

        return bw;
    }

    public BgoProtocolWriter writePlayerLogout(final long userID, final LocalDateTime logoutDate)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerLogout.value);
        bw.writeUInt32(userID);
        bw.writeLongDateTime(logoutDate);
        return bw;
    }

    public BgoProtocolWriter writePlayerMedal(final long userID, final MedalStatus medalStatus)
    {
        return this.writePlayerMedal(userID, medalStatus.pvpMedal(), medalStatus.tournamentMedal(),
                medalStatus.killerMedal(), medalStatus.assistMedal());
    }
    public BgoProtocolWriter writePlayerMedal(final long userID,
                                              final PvpMedal pvpMedal, final TournamentMedal tournamentMedal,
                                              final KillerMedal killerMedal, final AssistMedal assistMedal)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerMedal.value);
        bw.writeUInt32(userID);
        bw.writeByte(pvpMedal.getValue());
        bw.writeByte(tournamentMedal.getValue());
        bw.writeByte(killerMedal.getValue());
        bw.writeByte(assistMedal.getValue());
        return bw;
    }
    public BgoProtocolWriter writePlayerTitle(final long userID, final long titleGUID)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerTitle.value);
        bw.writeUInt32(userID);
        bw.writeGUID(titleGUID);
        return bw;
    }

    public BgoProtocolWriter writePlayerStatus(final long userID, final boolean isOnline)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerStatus.value);
        bw.writeUInt32(userID);
        bw.writeBoolean(isOnline);
        return bw;
    }


    public BgoProtocolWriter writePlayerLevel(final long userID, final short level)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerLevel.value);
        bw.writeUInt32(userID);
        bw.writeByte((byte) level);

        return bw;
    }

    public BgoProtocolWriter writePlayerLocation(final long playerID,
                                                 final GameLocation gameLocation, final long sectorGUID, final long roomGUID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerLocation.value);
        bw.writeUInt32(playerID);
        bw.writeByte(gameLocation.getValue());

        switch (gameLocation)
        {
            case Space, Story, Arena, BattleSpace, Tournament, Zone ->
            {
                bw.writeGUID(sectorGUID);
            }
            case Room ->
            {
                bw.writeGUID(sectorGUID);
                bw.writeGUID(roomGUID);
            }
            //dont write anything, client will just write 0 into sector & room guids
            default ->
            {}
        }

        return bw;
    }

    public BgoProtocolWriter writePlayerLocation(final long playerID, final Location location)
    {
        return this.writePlayerLocation(playerID, location.getGameLocation(), location.getSectorGUID(), location.getRoomGUID());
    }

    public BgoProtocolWriter writePlayerShips(final long userID, final HangarShipsUpdate hangarShipsUpdate)
    {
        return writePlayerShips(userID, hangarShipsUpdate.shipName(), hangarShipsUpdate.shipGuids());
    }
    public BgoProtocolWriter writePlayerShips(final long userID, final String shipName, final List<Long> shipGuids)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerShips.value);
        bw.writeUInt32(userID);
        bw.writeString(shipName);

        //write the shipGuids
        bw.writeUInt32Collection(shipGuids);

        return bw;
    }

    public BgoProtocolWriter writePlayerGuild(final long userID, final Guild guild) throws IllegalArgumentException, NullPointerException
    {
        Objects.requireNonNull(guild);

        final Optional<GuildMemberInfo> optMemberInfo = guild.getGuildMemberInfoOf(userID);
        if (optMemberInfo.isEmpty())
            throw new IllegalArgumentException("Could not fetch guild member info!");

        return writePlayerGuild(userID, guild.getId(), guild.getName(), optMemberInfo.get().getPlayerRole());
    }
    public BgoProtocolWriter writePlayerGuild(final long userID, final long guildID,
                                              final String guildName, final GuildRole guildRole)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerGuild.value);

        bw.writeUInt32(userID);
        bw.writeUInt32(guildID);
        bw.writeUInt32(guildRole.getValue());
        bw.writeString(guildName);

        return bw;
    }

    public BgoProtocolWriter writePlayerName(final long userID, final String userName)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerName.value);
        bw.writeUInt32(userID);
        bw.writeString(userName);
        return bw;
    }
    public BgoProtocolWriter writePlayerFaction(final long userID, final Faction faction)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerFaction.value);
        bw.writeUInt32(userID);
        bw.writeByte(faction.value);
        return bw;
    }
    public BgoProtocolWriter writePlayerAvatar(final long userID, final AvatarDescription avatarDescription)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(SubscribeProtocol.ServerMessage.PlayerAvatar.value);
        bw.writeUInt32(userID);
        bw.writeDesc(avatarDescription);
        return bw;
    }
}
