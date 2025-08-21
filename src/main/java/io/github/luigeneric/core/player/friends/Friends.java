package io.github.luigeneric.core.player.friends;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class Friends
{
    private final Set<Long> friends;
    private final Set<Long> friendInvites;

    public Friends()
    {
        this(new HashSet<>(), new HashSet<>());
    }

    /**
     * Removes the playerID to invite from the friends list
     * @param playerID id to remove
     * @return true if the invite was present
     */
    public boolean acceptFriendInvite(final long playerID)
    {
        synchronized (friendInvites)
        {
            return friendInvites.remove(playerID);
        }
    }

    /**
     * Adds a friend invite into the pending list
     * @param playerID player id to friend invites to add
     */
    public void addFriendInvited(final long playerID)
    {
        synchronized (friendInvites)
        {
            friendInvites.add(playerID);
        }
    }

    /**
     * No longer friends; remove friend from list
     * @param playerID id to remove
     * @return true if the player was present on the friendsList
     */
    public boolean removeFriend(final long playerID)
    {
        synchronized (friends)
        {
            return friends.remove(playerID);
        }
    }

    /**
     * Adds a player to the friends list
     *
     * @param playerID id to add to the friends list
     * @return false if the player was already present, true if the player was not a friend yet
     */
    public boolean addFriend(final long playerID)
    {
        synchronized (friends)
        {
            return friends.add(playerID);
        }
    }
}
