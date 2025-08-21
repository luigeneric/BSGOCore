package io.github.luigeneric.core.protocols.community;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.guild.*;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.enums.GuildOperation;
import io.github.luigeneric.enums.GuildRole;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class GuildProcessing
{
    private final UsersContainer usersContainer;
    private final GuildRegistry guildRegistry;
    private User user;
    private CommunityProtocol communityProtocol;
    private final CommunityProtocolWriteOnly writer;

    public GuildProcessing(UsersContainer usersContainer, GuildRegistry guildRegistry,
                           final CommunityProtocolWriteOnly writer)
    {
        this.usersContainer = usersContainer;
        this.guildRegistry = guildRegistry;
        this.writer = writer;
    }
    public void injectUser(final User user)
    {
        this.user = user;
        this.communityProtocol = user.getProtocol(ProtocolID.Community);
    }

    public void processMessage(final ClientMessage clientMessage, final BgoProtocolReader br) throws IOException
    {
        final Player currentPlayer = this.user.getPlayer();
        final Optional<Guild> optGuild = currentPlayer.getGuild();

        switch (clientMessage)
        {
            case GuildChangeRankName ->
            {
                final long uintRole = br.readUint32();
                final String roleName = br.readString();
                if (optGuild.isEmpty())
                {
                    log.error(user.getUserLog() + "GuildChangeRankName while guild was null");
                    return;
                }
                final Guild guild = optGuild.get();
                final boolean canChangeRankNames = guild.hasPermissions(currentPlayer.getUserID(), GuildOperation.ChangeRankNames);
                if (!canChangeRankNames)
                {
                    log.error(user.getUserLog() + "Change rank name but no permissions!");
                    return;
                }
                GuildRole guildRole = GuildRole.forValue((byte) uintRole);
                final boolean changeSuccessfully = guild.changeRankName(guildRole, roleName);
                if (!changeSuccessfully)
                    return;
                sendToEachGuildMember(guild, writer.writeGuildSetChangeRankName(guildRole, roleName));
            }
            case GuildChangeRankPermissions ->
            {
                final long uintRole = br.readUint32();
                final long newPermissions = br.readInt64();
                if (optGuild.isEmpty())
                {
                    log.error(user.getUserLog() + "ChangeRankPermissions while guild was null");
                    return;
                }
                final Guild guild = optGuild.get();
                final boolean canChangePermissions = guild.hasPermissions(currentPlayer.getUserID(), GuildOperation.ChangePermissions);
                if (!canChangePermissions)
                    return;
                final GuildRole role = GuildRole.forValue((byte) uintRole);
                if (role == null)
                    return;
                guild.changePermissionsOfRole(role, newPermissions);
                final BgoProtocolWriter changeRoleBw = communityProtocol.writer().writeGuildSetChangePermissions(role, newPermissions);
                sendToEachGuildMember(guild, changeRoleBw);
            }
            case GuildInvite ->
            {
                final long playerId = br.readUint32();
                if (optGuild.isEmpty())
                {
                    log.error(user.getUserLog() + "GuildInvite: guild was not present!");
                    return;
                }
                final Guild guild = optGuild.get();
                final boolean hasInvitePermissions = guild.hasPermissions(currentPlayer.getUserID(), GuildOperation.Invite);
                log.info(user.getUserLog() + "GuildInvite-> hasInvitePermissions: " + hasInvitePermissions);
                if (!hasInvitePermissions)
                    return;

                guildRegistry.getGuildSendToHistory().add(new GuildInvitedEntry(currentPlayer.getUserID(), playerId, guild.getId()));
                usersContainer.get(playerId).ifPresent(usr ->
                        usr.send(writer.writeGuildInvite(guild.getId(), guild.getName(), currentPlayer.getUserID())));
            }
            case GuildAccept ->
            {
                final long uintGuildId = br.readUint32();
                final long inviterId = br.readUint32();
                final boolean accepted = br.readBoolean();

                if (optGuild.isPresent())
                {
                    log.error(user.getUserLog() + " accepted guild invite but was already in first guild " + currentPlayer.getUserID());
                    return;
                }

                if (!accepted)
                    return;

                final Set<GuildInvitedEntry> invitingHistory = guildRegistry.getGuildSendToHistory();
                final boolean containedInvite = invitingHistory.remove(new GuildInvitedEntry(inviterId, currentPlayer.getUserID(), uintGuildId));
                if (!containedInvite)
                {
                    log.error(user.getUserLog() + "User tried to sneak into guild! userId " +
                            currentPlayer.getUserID() + " guildID " + uintGuildId + " inviterId which didnt send "+ inviterId);
                    return;
                }

                final Optional<Guild> optAcceptGuild = guildRegistry.getGuild(uintGuildId);
                if (optAcceptGuild.isEmpty())
                {
                    log.error(user.getUserLog() + "Accepted guild was not present in guildRegistry!");
                    return;
                }
                final Guild acceptedGuild = optAcceptGuild.get();
                acceptedGuild.addMember(this.user.getPlayer(), GuildRole.Recruit);
                currentPlayer.setGuild(acceptedGuild);
                this.user.send(writer.writeGuildInfo(new GuildInfoMessage(acceptedGuild)));
                final Optional<GuildMemberInfo> optMemberInfo = acceptedGuild.getGuildMemberInfoOf(currentPlayer.getUserID());
                optMemberInfo.ifPresent(memberInfo ->
                {
                    sendToEachGuildMember(acceptedGuild, writer.writeGuildMemberUpdate(memberInfo));
                });
            }
            case GuildKick ->
            {
                final long playerToKick = br.readUint32();
                if (optGuild.isEmpty())
                    return;
                final Guild guild = optGuild.get();
                final Optional<GuildMemberInfo> optGuildMemberInfo = guild.getGuildMemberInfoOf(playerToKick);
                if (optGuildMemberInfo.isEmpty())
                {
                    log.warn("{} GuildKick, user to kick is empty!", user.getUserLog());
                    return;
                }
                final Optional<User> optUserToKick = usersContainer.get(playerToKick);
                optUserToKick.ifPresent(usrToKick ->
                {
                    usrToKick.getPlayer().setGuild(null);
                    usrToKick.send(writer.writeGuildRemove(usrToKick.getPlayer().getUserID(), false));
                });


                final boolean hasKickPermissions = guild.hasPermissions(currentPlayer.getUserID(), GuildOperation.KickMember);
                if (!hasKickPermissions)
                    log.error(user.getUserLog() + "KickPermissions not satisfised!");
                guild.removePlayer(playerToKick);

                final BgoProtocolWriter guildKickBw = writer.writeGuildRemove(playerToKick, false);
                sendToEachGuildMember(guild, guildKickBw);
            }
            case GuildLeave ->
            {
                if (optGuild.isEmpty())
                    return;
                final Guild guild = optGuild.get();
                final boolean wasPresent = guild.removePlayer(currentPlayer.getUserID());
                if (!wasPresent)
                {
                    log.error(user.getUserLog() + "Remove player from guild, but player was not present!");
                    return;
                }

                final BgoProtocolWriter guildLeaveBw = writer.writeGuildRemove(currentPlayer.getUserID(), true);
                sendToEachGuildMember(guild, guildLeaveBw);
                this.user.send(writer.writeGuildQuit());
                guild.removePlayer(currentPlayer.getUserID());
                final GuildMemberInfo newGuildMemberInfo = guild.appointNewLeaderIfLeaderNotPresent();
                if (newGuildMemberInfo != null)
                {
                    sendToEachGuildMember(guild, writer.writeGuildMemberUpdate(newGuildMemberInfo));
                }
                currentPlayer.setGuild(null);
            }
            case GuildPromote ->
            {
                final long playerId = br.readUint32();
                final long role = br.readUint32();
                if (optGuild.isEmpty())
                {
                    log.error(user.getUserLog() + "Guild promote player but player has no guild to promote first user!");
                    return;
                }
                final Guild guild = optGuild.get();
                final Optional<GuildMemberInfo> optPromotingPlayer = guild.getGuildMemberInfoOf(currentPlayer.getUserID());
                if (optPromotingPlayer.isEmpty())
                {
                    log.error(user.getUserLog() + "Guild member info of optPromotingPlayer was null");
                    return;
                }
                final Optional<GuildMemberInfo> optToPromoteDemoteMember = guild.getGuildMemberInfoOf(playerId);
                if (optToPromoteDemoteMember.isEmpty())
                {
                    log.error(user.getUserLog() + "Guild member info of optToPromoteDemoteMember was null");
                    return;
                }
                final GuildMemberInfo memberToPromoteOrDemote = optToPromoteDemoteMember.get();
                final GuildRole newRole = GuildRole.forValue((byte) role);
                if (newRole == null)
                {
                    log.error("New Role was null");
                    return;
                }
                memberToPromoteOrDemote.setPlayerRole(newRole);
                final GuildMemberPromotionMessage guildMemberPromotionMessage = new GuildMemberPromotionMessage(playerId, newRole);
                final BgoProtocolWriter bw = writer.writeGuildSetPromotion(guildMemberPromotionMessage);
                sendToEachGuildMember(guild, bw);
            }
            case GuildStart ->
            {
                final String guildName = br.readString();
                //check if name is ok, no nazi symbols, bad words, etc.
                if (optGuild.isPresent())
                {
                    log.warn(user.getUserLog() + "GuildStart but user already has first guild");
                    return;
                }
                try
                {
                    final Guild newGuild = this.guildRegistry.createGuildIfNotExists(guildName);
                    log.info(user.getUserLog() + "NewGuild-Create successfully " + guildName);
                    newGuild.addMember(user.getPlayer(), GuildRole.Leader);
                    currentPlayer.setGuild(newGuild);

                    this.user.send(writer.writeGuildInfo(new GuildInfoMessage(newGuild)));
                }
                //Guild already exists
                catch (IllegalArgumentException illegalArgumentException)
                {
                    final BgoProtocolWriter bw = communityProtocol.writer().writeGuildStartError(guildName);
                    this.user.send(bw);
                }
            }
        }
    }

    public void sendToEachGuildMember(final Guild guild, final BgoProtocolWriter bw)
    {
        for (final GuildMemberInfo guildMemberInfo : guild.getGuildMemberInfos())
        {
            usersContainer.get(guildMemberInfo.getPlayerID()).ifPresent(user -> user.send(bw));
        }
    }
}