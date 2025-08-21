package io.github.luigeneric.core.protocols.community;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.community.party.PartyInviteEntry;
import io.github.luigeneric.core.community.party.PartyRegistry;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.PartyMemberFtlState;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class PartyProcessing
{
    private final UsersContainer usersContainer;
    private final PartyRegistry partyRegistry;
    private User user;
    private CommunityProtocol communityProtocol;
    private final CommunityProtocolWriteOnly writer;

    public PartyProcessing(final UsersContainer usersContainer,
                           final PartyRegistry partyRegistry,
                           final CommunityProtocolWriteOnly writer)
    {
        this.usersContainer = usersContainer;
        this.partyRegistry= partyRegistry;
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
        final Optional<IParty> optCurrentParty = currentPlayer.getParty();
        switch (clientMessage)
        {
            case PartyMemberFtlState ->
            {
                final long[] partyMemberIds = br.readUint32Array();
                if (optCurrentParty.isEmpty())
                {
                    log.error(user.getUserLog() + "PartyMemberFTLState but no party!");
                    return;
                }
                final IParty party = optCurrentParty.get();
                final boolean oneOfTheIdsIsNotInParty = Arrays.stream(partyMemberIds)
                        .anyMatch(id -> !party.isInParty(id));
                if (oneOfTheIdsIsNotInParty)
                {
                    log.error(user.getUserLog() + "One of the ids is not in the party!");
                    return;
                }
                final List<User> partyJumpUsers = party.getPartyUsersByIds(partyMemberIds);
                final List<PartyMemberFtlRecord> ftlRecords = new ArrayList<>();
                final Location initLocation = currentPlayer.getLocation();
                for (final User partyJumpUser : partyJumpUsers)
                {
                    final boolean sameSector = partyJumpUser.getPlayer().getLocation().getSectorID() == initLocation.getSectorID();
                    if (!sameSector)
                        continue;
                    final boolean partyMemberInSpace = partyJumpUser.getPlayer().getLocation().getGameLocation() == GameLocation.Space;
                    if (!partyMemberInSpace)
                        continue;
                    final boolean partyMemberIsOnline = partyJumpUser.getConnection().isPresent();
                    if (!partyMemberIsOnline)
                        continue;
                    ftlRecords.add(new PartyMemberFtlRecord(partyJumpUser, PartyMemberFtlState.Ready));
                }
                user.send(writer.writePartyMemberFtlState(ftlRecords));
            }
            case PartyChatInvite ->
            {
                final String userNameToInvite = br.readString();
                log.info(user.getUserLog() + "PartyChatInvite: " + userNameToInvite);
                final Optional<User> optUserInv = this.getUser(userNameToInvite);
                if (optUserInv.isEmpty())
                {
                    log.warn(user.getUserLog() + "UserInvite by Name but no user exists!");
                    return;
                }
                partyInvite(optUserInv.get());
            }
            case PartyInvitePlayer ->
            {
                final long userID = br.readUint32();
                log.info(user.getUserLog() + "PartyPlayerInvite: " + userID);
                final Optional<User> optUserInv = this.getUser(userID);
                if (optUserInv.isEmpty())
                {
                    log.warn(user.getUserLog() + "UserInvite but no user exists!");
                    return;
                }
                partyInvite(optUserInv.get());
            }
            case PartyAppointLeader ->
            {
                final long newLeaderID = br.readUint32();
                if (optCurrentParty.isEmpty())
                {
                    log.warn(user.getUserLog() + "Party new leader but player has no party");
                    return;
                }
                final IParty party = optCurrentParty.get();
                final Player leaderChar = party.getLeader().getPlayer();
                if (leaderChar.getUserID() == newLeaderID)
                {
                    log.warn(user.getUserLog() + "New LeaderID is same as before");
                    return;
                }
                party.appointNewLeader(newLeaderID);
                sendToParty(party, communityProtocol.writer().writeParty(party));

                //check if users are in party even thou they are not from the same faction

                final boolean isAnyNotSameFaction = party.getMembersCopy()
                        .stream()
                        .anyMatch(member -> member.getPlayer().getFaction() != user.getPlayer().getFaction());
                if (isAnyNotSameFaction)
                {
                    party.getMembers()
                            .forEach(member -> member.getConnection()
                                    .ifPresent(abstractConnection ->
                                            abstractConnection.closeConnection("Atleast one of the members are not in the same faction")
                                    )
                            );
                }
            }
            case PartyAccept ->
            {
                final long partyId = br.readUint32();
                final long inviterId = br.readUint32();
                final boolean accept = br.readBoolean();

                final boolean containedInvite = this.partyRegistry.getPartySendToHistory()
                        .remove(new PartyInviteEntry(inviterId, user.getPlayer().getUserID(), partyId));
                if (!containedInvite)
                {
                    log.error(user.getUserLog() + "User tried to sneak into party! userId " +
                            currentPlayer.getUserID() + " party " + partyId + " inviterId which didnt send "+ inviterId);
                    return;
                }

                //user already in party
                if (currentPlayer.getParty().isPresent())
                {
                    log.info(user.getUserLog() + "User was already in party");
                    return;
                }

                final Optional<User> optInviter = this.getUser(inviterId);
                if (optInviter.isEmpty())
                {
                    log.info(user.getUserLog() + "Inviter is null");
                    return;
                }


                final User inviter = optInviter.get();
                final Player inviterChar = inviter.getPlayer();
                final Optional<IParty> optInviterParty = inviterChar.getParty();
                if (optInviterParty.isEmpty())
                {
                    log.warn(user.getUserLog() + "PartyAccept but inviter-party is null");
                    return;
                }
                final IParty inviterParty = optInviterParty.get();
                if (inviterParty.getPartyId() != partyId)
                {
                    log.warn(user.getUserLog() + "Party accept but partyID was not equal!");
                    return;
                }
                if (!accept)
                {
                    inviter.send(writer.writePartyIgnore(currentPlayer.getName(), false));
                    if (inviterParty.getMemberCount() < 2)
                    {
                        partyRegistry.removeParty(inviterParty.getPartyId());
                        inviterChar.setParty(null);
                    }
                    return;
                }
                //adds the client to the party and sets the client party
                inviterParty.addMember(this.user);

                sendToParty(inviterParty, communityProtocol.writer().writeParty(inviterParty));
            }
            case PartyDismissPlayer ->
            {
                final long playerId = br.readUint32();
                if (optCurrentParty.isEmpty())
                {
                    log.warn(user.getUserLog() + "DismissPlayer but no party exists");
                    return;
                }
                final IParty currentParty = optCurrentParty.get();
                if (currentParty.getLeader().getPlayer().getUserID() != currentPlayer.getUserID())
                {
                    log.warn(user.getUserLog() + "Dismiss request but user is not leader");
                    return;
                }
                final User memberRemoved = currentParty.removeMember(playerId);
                memberRemoved.send(communityProtocol.writer().writeNoParty());
                if (currentParty.getMemberCount() < 2)
                {
                    currentParty.removeMember(user);
                    partyRegistry.removeParty(currentParty);
                    user.send(communityProtocol.writer().writeNoParty());
                }
                else
                {
                    sendToParty(currentParty, communityProtocol.writer().writeParty(currentParty));
                }
            }
            case PartyLeave ->
            {
                if (optCurrentParty.isEmpty())
                {
                    log.warn(user.getUserLog() + "Client send leave party but was not in party");
                    return;
                }
                final IParty party = optCurrentParty.get();
                removeUserFromParty(user, party);
            }
        }
    }

    public void removeUserFromParty(final User user, final IParty party)
    {
        final User removedUser = party.removeMember(user);
        user.send(communityProtocol.writer().writeNoParty());
        //the other one has no party as well
        if (party.getMemberCount() < 2)
        {
            sendToParty(party, communityProtocol.writer().writeNoParty());
            for (final User member : party.getMembers())
            {
                party.removeMember(member);
            }
            partyRegistry.removeParty(party.getPartyId());
        }
        else
        {
            final BgoProtocolWriter sendParty = communityProtocol.writer().writeParty(party);
            sendToParty(party, sendParty);
        }
    }

    private void sendToParty(final IParty party, final BgoProtocolWriter bw)
    {
        for (final User member : party.getMembers())
        {
            member.send(bw);
        }
    }


    private Optional<User> getUser(final long userID)
    {
        return this.usersContainer.get(userID);
    }
    private Optional<User> getUser(final String name)
    {
        return this.usersContainer.get(name);
    }

    private void partyInvite(final User userToInvite)
    {
        final Player currentPlayer = user.getPlayer();
        final Player otherPlayer = userToInvite.getPlayer();

        if (currentPlayer.getFaction() != otherPlayer.getFaction())
        {
            log.warn("User invited other faction from {} other {}", currentPlayer.getPlayerLog(), otherPlayer.getPlayerLog());
            return;
        }

        final Optional<IParty> currentOptParty = currentPlayer.getParty();
        final Optional<IParty> otherOptParty = otherPlayer.getParty();

        //other is already in party
        if (otherOptParty.isPresent())
        {
            user.send(writer.writePartyIgnore(otherPlayer.getName(), true));
        }
        //user has no party yet, send party request to user
        else
        {
            final IParty currentParty = currentOptParty.orElseGet(() -> this.partyRegistry.createNewParty(user));
            user.getPlayer().setParty(currentParty);
            partyRegistry.getPartySendToHistory()
                    .add(new PartyInviteEntry(user.getPlayer().getUserID(), otherPlayer.getUserID(), currentParty.getPartyId()));
            userToInvite.send(writer.writePartyInvite(currentParty));
        }
    }

    public void clearInvites()
    {
        this.partyRegistry
                .getPartySendToHistory()
                .removeIf(partyInviteEntry -> partyInviteEntry.inviter() == user.getPlayer().getUserID());
    }
}
