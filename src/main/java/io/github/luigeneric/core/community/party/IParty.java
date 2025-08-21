package io.github.luigeneric.core.community.party;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;

import java.util.Collection;
import java.util.List;

public interface IParty
{
    int MAX_SIZE = 10;
    long getPartyId();

    User getLeader();

    Collection<User> getMembersWithoutLeader();

    /**
     * Adds a new member to the party
     * @param newMember
     * @return true if member is properly added to the party, false if error
     * @throws NullPointerException
     */
    boolean addMember(final User newMember) throws NullPointerException;
    User removeMember(final User user);
    User removeMember(final long userId);

    void appointNewLeader(final long userID) throws IllegalArgumentException;

    int getMemberCount();

    Collection<User> getMembers();
    Collection<User> getMembersCopy();

    boolean isInParty(final long userID);

    List<User> getPartyUsersByIds(final long[] partyMemberIds);

    boolean isEmpty();

    void sendToAllMembers(final BgoProtocolWriter partyAnchorBuffer);
}
