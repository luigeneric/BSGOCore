package io.github.luigeneric.core.community.party;


import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.AbstractConnection;
import io.github.luigeneric.core.User;
import io.quarkus.arc.Lock;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ApplicationScoped
public class PartyRegistry implements Runnable
{
    private final Map<Long, IParty> parties;
    private final Set<PartyInviteEntry> partySendToHistory;

    public PartyRegistry()
    {
        this.parties = new HashMap<>();
        this.partySendToHistory = new CopyOnWriteArraySet<>();
    }

    @Lock(Lock.Type.READ)
    public IParty getPartyById(final long id)
    {
        return this.parties.get(id);
    }


    private long getNextFreeID()
    {
        for (long i = 1; i < (Integer.MAX_VALUE * 2L + 1); i++)
        {
            if (!this.parties.containsKey(i))
            {
                return i;
            }
        }
        throw new IllegalStateException("Can not add, no free ID found");
    }

    @Lock
    public IParty createNewParty(final User leader)
    {
        final long nextFreeID = this.getNextFreeID();
        final IParty party = new Party(nextFreeID, leader);
        this.parties.put(nextFreeID, party);
        return party;
    }

    @Override
    @Lock
    public void run()
    {
        //checking for the party if all group members are offline
        final List<IParty> partiesToRemove = new ArrayList<>();
        for (final IParty party : this.parties.values())
        {
            final List<User> membersToRemove = new ArrayList<>();
            for (final User member : party.getMembers())
            {
                final Optional<AbstractConnection> optConnection = member.getConnection();
                if (optConnection.isPresent())
                {
                    continue;
                }
                final Optional<BgoTimeStamp> optLastLogout = member.getPlayer().getLastLogout();
                if (optLastLogout.isEmpty())
                    continue;
                final BgoTimeStamp lastLogout = optLastLogout.get();
                //is logoutDate 2 minutes or more since now
                if (lastLogout.getLocalDate().plusMinutes(2).isBefore(LocalDateTime.now(Clock.systemUTC())))
                {
                    membersToRemove.add(member);
                }
            }
            membersToRemove.forEach(party::removeMember);
            if (party.isEmpty())
            {
                partiesToRemove.add(party);
            }
        }

        for (final IParty party : partiesToRemove)
        {
            this.removePartyInternal(party.getPartyId());
        }
    }

    public void removeParty(final IParty party)
    {
        this.removeParty(party.getPartyId());
    }
    @Lock
    public void removeParty(final long partyId)
    {
        removePartyInternal(partyId);
    }
    private void removePartyInternal(final long partyId)
    {
        this.parties.remove(partyId);
    }

    public Set<PartyInviteEntry> getPartySendToHistory()
    {
        return partySendToHistory;
    }
}
