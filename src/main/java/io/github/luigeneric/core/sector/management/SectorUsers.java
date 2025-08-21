package io.github.luigeneric.core.sector.management;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;

import java.util.*;


/**
 * Map: UserID, User
 * Map: UserID, PlayerShip
 */
public class SectorUsers
{
    /**
     * userID, User
     */
    private final Map<Long, User> usersByUserId;

    /**
     * userID, PlayerShip
     */
    private final Map<Long, PlayerShip> playerShipByUserId;
    /**
     * objectID, PlayerShip
     */
    private final Map<Long, PlayerShip> playerShipsByObjectId;
    /**
     * userid present if first spaceobject was spawned
     */
    private final Set<Long> userIdsWithSpawnedObjects;

    public SectorUsers(final Map<Long, User> usersByUserId,
                       final Map<Long, PlayerShip> playerShipByUserId,
                       final Map<Long, PlayerShip> playerShipsByObjectId,
                       final Set<Long> userIdsWithSpawnedObjects
    )
    {
        this.usersByUserId = usersByUserId;
        this.playerShipByUserId = playerShipByUserId;
        this.playerShipsByObjectId = playerShipsByObjectId;
        this.userIdsWithSpawnedObjects = userIdsWithSpawnedObjects;
    }
    public SectorUsers()
    {
        this(new LinkedHashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>());
    }

    protected void add(final User user, final PlayerShip playerShip)
    {
        final long userID = user.getPlayer().getUserID();
        this.usersByUserId.put(userID, user);
        this.playerShipByUserId.put(userID, playerShip);
        this.playerShipsByObjectId.put(playerShip.getObjectID(), playerShip);
    }
    protected User remove(final long userID)
    {
        if (userID < 0)
            return null;

        final User removedUser = this.usersByUserId.remove(userID);
        final PlayerShip removedShip = this.playerShipByUserId.remove(userID);
        if (removedShip != null)
            this.playerShipsByObjectId.remove(removedShip.getObjectID());
        this.removeUserFromJoinFlag(userID);
        return removedUser;
    }

    public Optional<User> getUser(final SpaceObject playerShip)
    {
        if (!playerShip.isPlayer())
            return Optional.empty();

        return this.getUser(playerShip.getPlayerId());
    }
    public Optional<User> getUser(final long userID)
    {
        return Optional.ofNullable(this.usersByUserId.get(userID));
    }
    public User getUserUnsafe(final long userID)
    {
        return this.usersByUserId.get(userID);
    }
    public PlayerShip getPlayerShipUnsafe(final long userID)
    {
        return this.playerShipByUserId.get(userID);
    }
    public Optional<PlayerShip> getPlayerShipByUserID(final long userID)
    {
        return Optional.ofNullable(this.playerShipByUserId.get(userID));
    }

    public boolean isEmpty()
    {
        return this.usersByUserId.isEmpty();
    }

    public Collection<User> getUsersCollection()
    {
        return this.usersByUserId.values();
    }
    public long getUserCntBasedOnFaction(final Faction faction)
    {
        return switch (faction)
        {
            case Cylon,Colonial -> new ArrayList<>(this.usersByUserId.values())
                    .stream()
                    .filter(user -> user.getPlayer().getFaction() == faction)
                    .count();
            case Neutral, Ancient -> this.usersByUserId.size();
        };
    }
    public List<User> getUsers()
    {
        return usersByUserId.values().stream().toList();
    }
    public List<PlayerShip> getPlayerShips()
    {
        return List.copyOf(this.playerShipByUserId.values());
    }
    public Collection<PlayerShip> getPlayerShipsCollection()
    {
        return this.playerShipByUserId.values();
    }

    public Optional<PlayerShip> getPlayerShipBySpaceObjectID(final long objectID)
    {
        return Optional.ofNullable(this.playerShipsByObjectId.get(objectID));
    }

    /**
     * Adds an user to the set which indicates that first spaceobject for the user already exists
     *
     * @param userID
     */
    public void addUserJoinedSpaceObjects(final long userID)
    {
        this.userIdsWithSpawnedObjects.add(userID);
    }
    protected void removeUserFromJoinFlag(final long userID)
    {
        this.userIdsWithSpawnedObjects.remove(userID);
    }
}
