package io.github.luigeneric.core.community.party;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.utils.AutoLock;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class Party implements IParty
{

    private final long partyId;
    private long leaderId;
    private final Map<Long, User> members;
    private final Lock lock;


    protected Party(final long partyID, final User leader)
    {
        Objects.requireNonNull(leader, "PartyLeader cannot be null!");
        this.partyId = partyID;
        this.members = new ConcurrentHashMap<>();
        this.leaderId = leader.getPlayer().getUserID();
        this.lock = new ReentrantLock();
        this.addMember(leader);
    }

    @Override
    public boolean addMember(final User newMember) throws IllegalArgumentException, NullPointerException
    {
        Objects.requireNonNull(newMember, "New party member cannot be null!");


        try(var autoLock = new AutoLock(lock))
        {
            log.info("Party add member at stats leaderId={}, partySize={}, membersIds={}",
                    leaderId,
                    getMemberCount(),
                    members.values().stream().map(usr -> usr.getPlayer().getUserID()).collect(Collectors.toSet())
            );

            if (members.size() >= 10)
            {
                log.warn("User {} tried to invite more than 10 users", getLeader().getUserLog());
                return false;
            }

            if (this.members.containsKey(newMember.getPlayer().getUserID()))
            {
                return false;
            }
            newMember.getPlayer().setParty(this);
            this.members.put(newMember.getPlayer().getUserID(), newMember);
            return true;
        }
    }
    @Override
    public List<User> getPartyUsersByIds(final long... ids)
    {
        try(var autoLock = new AutoLock(lock))
        {
            final List<User> users = new ArrayList<>(ids.length);
            for (final long id : ids)
            {
                users.add(this.members.get(id));
            }
            return users;
        }
    }
    @Override
    public User removeMember(final User memberToRemove)
    {
        Objects.requireNonNull(memberToRemove, "Remove member must be non null");
        return this.removeMember(memberToRemove.getPlayer().getUserID());
    }
    @Override
    public User removeMember(final long userID)
    {
        lock.lock();
        try
        {
            final User rv = this.members.remove(userID);
            final boolean removedIsLeader = leaderId == userID;
            if (removedIsLeader && members.size() >= 2)
            {
                members.values()
                        .stream()
                        .findAny()
                        .ifPresent(leader -> appointNewLeader(leader.getPlayer().getUserID()));
            }
            rv.getPlayer().setParty(null);
            return rv;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void appointNewLeader(final long userID) throws IllegalArgumentException
    {
        lock.lock();
        try
        {
            if (this.leaderId == userID)
                return;

            if (!this.members.containsKey(userID))
            {
                throw new IllegalArgumentException("Could not find userID " + userID + " in party to appoint him as new leader");
            }

            this.leaderId = userID;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public int getMemberCount()
    {
        return this.members.size();
    }

    @Override
    public long getPartyId()
    {
        return this.partyId;
    }


    @Override
    public Collection<User> getMembers()
    {
        lock.lock();
        try
        {
            return this.members.values();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public Collection<User> getMembersCopy()
    {
        lock.lock();
        try
        {
            return new ArrayList<>(this.members.values());
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public Collection<User> getMembersWithoutLeader()
    {
        //get check can be proceed because in add and remove is everything okay
        lock.lock();
        try
        {
            return this.members.values().stream().filter(member -> member.getPlayer().getUserID() != leaderId).toList();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public User getLeader()
    {
        lock.lock();
        try
        {
            return this.members.get(this.leaderId);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean isInParty(final long userID)
    {
        if (userID == -1)
            return false;
        lock.lock();
        try
        {
            return this.members.containsKey(userID);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty()
    {
        lock.lock();
        try
        {
            return this.members.isEmpty();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void sendToAllMembers(final BgoProtocolWriter partyAnchorBuffer)
    {
        this.lock.lock();
        try
        {
            for (final User user : this.members.values())
            {
                user.send(partyAnchorBuffer);
            }
        }
        finally
        {
            this.lock.unlock();
        }
    }

}
