package io.github.luigeneric.core.protocols.community;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.community.guild.*;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.enums.GuildRole;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommunityProtocolWriteOnly extends WriteOnlyProtocol
{
    public CommunityProtocolWriteOnly()
    {
        super(ProtocolID.Community);
    }


    public BgoProtocolWriter writeParty(final IParty party)
    {
        if (party == null)
            return writeNoParty();

        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Party.intValue);

        //partyID
        bw.writeUInt32(party.getPartyId());
        //playerID of the leader
        bw.writeUInt32(party.getLeader().getPlayer().getUserID());

        final Set<Long> membersIdsWithoutLeader = party.getMembersWithoutLeader().stream()
                .map(member -> member.getPlayer().getUserID())
                .collect(Collectors.toSet());
        bw.writeUInt32Collection(membersIdsWithoutLeader);

        //carrier ids
        bw.writeLength(0); //temp

        return bw;
    }

    public BgoProtocolWriter writeNoRecruits()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Recruits.intValue);
        bw.writeLength(0);
        return bw;
    }
    protected BgoProtocolWriter writeRequiredRecruitLevel(final long level)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.RecruitLevel.intValue);
        bw.writeUInt32(level);
        return bw;
    }

    /**
     *
     * @param chatSessionID
     * @param chatProjectID
     * @param chatLanguage
     * @param chatServerUrl ip address
     * @return
     */
    public BgoProtocolWriter writeChatSessionId(final String chatSessionID, final long chatProjectID, final String chatLanguage, final String chatServerUrl)
    {
        final BgoProtocolWriter bw = newMessage();

        bw.writeMsgType(ServerMessage.ChatSessionId.intValue);
        bw.writeString(chatSessionID);
        bw.writeUInt32(chatProjectID);
        bw.writeString(chatLanguage);
        bw.writeString(chatServerUrl);

        return bw;
    }

    public BgoProtocolWriter writeNoParty()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Party.intValue);

        //partyID
        bw.writeUInt32(0);
        //leaderID
        bw.writeUInt32(0);

        //no users
        bw.writeLength(0);
        //no carriers
        bw.writeLength(0);

        return bw;
    }

    public BgoProtocolWriter writeGuildSetChangePermissions(final GuildRole guildRole, long newPermissions)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildSetChangePermissions.intValue);
        bw.writeByte(guildRole.getValue());
        bw.writeUInt64(newPermissions);

        return bw;
    }

    public BgoProtocolWriter writeGuildStartError(final String nameAlreadyTaken)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildStartError.intValue);
        bw.writeString(nameAlreadyTaken);

        return bw;
    }

    public BgoProtocolWriter writeGuildJoinError(final String requestedWingName, final CommunityProtocol.GuildJoinError guildJoinError)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildJoinError.intValue);
        bw.writeString(requestedWingName);
        bw.writeByte(guildJoinError.getByte());

        return bw;
    }

    public BgoProtocolWriter writePartyIgnore(final String characterName, final boolean isAlreadyInSquad)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PartyIgnore.intValue);

        bw.writeString(characterName);
        if (isAlreadyInSquad)
        {
            bw.writeByte((byte) 1);
        }
        //just declined party invite
        else
        {
            bw.writeByte((byte) 0);
        }

        return bw;
    }

    public BgoProtocolWriter writePartyInvite(final IParty party)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PartyInvite.intValue);

        bw.writeUInt32(party.getPartyId());
        bw.writeUInt32(party.getLeader().getPlayer().getUserID());
        bw.writeString(party.getLeader().getPlayer().getName());

        return bw;
    }


    public BgoProtocolWriter writeGuildQuit()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildQuit.intValue);

        return bw;
    }

    public BgoProtocolWriter writeGuildRemove(final long playerID, final boolean isSelfLeave)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildRemove.intValue);

        bw.writeUInt32(playerID);
        bw.writeBoolean(isSelfLeave);

        return bw;
    }
    public BgoProtocolWriter writeGuildInvite(final long guildID, final String guildName, final long inviterID)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildInvite.intValue);

        bw.writeUInt32(guildID);
        bw.writeString(guildName);
        bw.writeUInt32(inviterID);

        return bw;
    }
    public BgoProtocolWriter writeGuildInfo(final GuildInfoMessage guildInfoMessage)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildInfo.intValue);
        bw.writeDesc(guildInfoMessage);

        return bw;
    }

    public BgoProtocolWriter writeGuildSetPromotion(final GuildMemberPromotionMessage guildMemberPromotionMessage)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildSetPromotion.intValue);
        bw.writeDesc(guildMemberPromotionMessage);

        return bw;
    }

    public BgoProtocolWriter writeGuildMemberUpdate(final GuildMemberInfo newGuildMemberInfo)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildMemberUpdate.intValue);
        bw.writeDesc(newGuildMemberInfo);

        return bw;
    }

    public BgoProtocolWriter writeGuildSetChangeRankName(final GuildRole guildRole, final String newRankName)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildSetChangeRankName.intValue);
        bw.writeByte(guildRole.getValue());
        bw.writeString(newRankName);

        return bw;
    }



    public BgoProtocolWriter writeGuildInviteResult(final GuildInviteResultMessage guildInviteResultMessage)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildInviteResult.intValue);
        bw.writeDesc(guildInviteResultMessage);

        return bw;
    }
    public BgoProtocolWriter writeGuildOperationResult(final GuildOperationResultMessage guildOperationResultMessage)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.GuildOperationResult.intValue);
        bw.writeDesc(guildOperationResultMessage);

        return bw;
    }

    public BgoProtocolWriter writePartyChatInviteFailed(final String userNameToInvite)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PartyChatInviteFailed.intValue);
        bw.writeString(userNameToInvite);

        return bw;
    }

    public BgoProtocolWriter writePartyMemberFtlState(final List<PartyMemberFtlRecord> partyMemberFtlStates)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PartyMemberFtlState.intValue);
        bw.writeLength(partyMemberFtlStates.size());

        for (final PartyMemberFtlRecord entry : partyMemberFtlStates)
        {
            bw.writeUInt32(entry.user().getPlayer().getUserID());
            bw.writeByte(entry.state().getValue());
        }
        return bw;
    }

    public BgoProtocolWriter writePartyAnchor(final long carrierPlayerId, final long anchorPlayerId, final boolean isAnchored)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PartyAnchor.intValue);

        bw.writeUInt32(carrierPlayerId);
        bw.writeUInt32(anchorPlayerId);
        bw.writeBoolean(isAnchored);

        return bw;
    }
}
