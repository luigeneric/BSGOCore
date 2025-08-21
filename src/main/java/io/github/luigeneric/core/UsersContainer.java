package io.github.luigeneric.core;


import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.player.NameValidation;
import io.github.luigeneric.enums.Faction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class UsersContainer
{
    private final Map<Long, User> userMap;
    private final Map<Long, PlayerlessUser> usersWithoutCharacter;
    @Getter
    private final AtomicLong colonialCount;
    @Getter
    private final AtomicLong cylonCount;
    @Getter
    private final AtomicLong colonialSumLevel;
    @Getter
    private final AtomicLong cylonSumLevel;
    private final NameValidation nameValidation;
    private final ReadWriteLock readWriteLock;


    @Inject
    public UsersContainer(final NameValidation nameValidation)
    {
        this(new LinkedHashMap<>(), new HashMap<>(), nameValidation);
    }
    private UsersContainer(final Map<Long, User> userMap, final Map<Long, PlayerlessUser> usersWithoutCharacter,
                           final NameValidation nameValidation)
    {
        this.userMap = userMap;
        this.usersWithoutCharacter = usersWithoutCharacter;
        this.nameValidation = nameValidation;
        this.colonialCount = new AtomicLong();
        this.cylonCount = new AtomicLong();
        this.colonialSumLevel = new AtomicLong();
        this.cylonSumLevel = new AtomicLong();
        this.readWriteLock = new ReentrantReadWriteLock();
    }


    private void lockRead()
    {
        this.readWriteLock.readLock().lock();
    }
    private void lockWrite()
    {
        this.readWriteLock.writeLock().lock();
    }
    private void releaseRead()
    {
        this.readWriteLock.readLock().unlock();;
    }
    private void releaseWrite()
    {
        this.readWriteLock.writeLock().unlock();
    }

    public boolean checkNameFree(final String nameToCheck, final long userId)
    {
        lockRead();
        try
        {
            if (getInternal(nameToCheck).isPresent())
            {
                return false;
            }
            return nameValidation.checkNameIsFree(nameToCheck, userId);
        }
        finally
        {
            releaseRead();
        }
    }
    public synchronized boolean chooseNameIfPresentInReservation(final String name, final long playerId)
    {
        return this.nameValidation.removeReservation(name, playerId);
    }
    public boolean containsUser(final long id)
    {
        lockRead();
        try
        {
            return this.userMap.containsKey(id);
        }
        finally
        {
            releaseRead();
        }
    }

    private Optional<PlayerlessUser> getWithoutCharInternal(final long id)
    {
        return Optional.ofNullable(this.usersWithoutCharacter.get(id));
    }
    public Optional<User> get(final long id)
    {
        lockRead();
        try
        {
            return Optional.ofNullable(this.userMap.get(id));
        }
        finally
        {
            releaseRead();
        }
    }

    private Optional<User> getInternal(final String name)
    {
        if (name == null)
            return Optional.empty();

        return this.userMap.values().stream()
                .filter(user ->
                {
                    Player player;
                    if ((player = user.getPlayer()) != null)
                    {
                        return player.getName().equals(name);
                    }
                    return false;
                })
                .findAny();
    }
    public Optional<User> get(final String name)
    {
        lockRead();
        try
        {
            return getInternal(name);
        }
        finally
        {
            releaseRead();
        }
    }

    public int size()
    {
        lockRead();
        try
        {
            return this.userMap.size();
        }
        finally
        {
            releaseRead();
        }
    }

    @Scheduled(every = "5s")
    public void update()
    {
        final Stream<User> colonialStream = this.userStream(user -> user.getPlayer().getFaction() == Faction.Colonial);
        final Stream<User> cylonStream = this.userStream(user -> user.getPlayer().getFaction() == Faction.Cylon);
        final long currentSumColonialLevel = colonialStream
                .map(user -> user.getPlayer().getSkillBook().get())
                .mapToLong(Short::longValue).sum();
        this.colonialSumLevel.set(currentSumColonialLevel);
        final long currentSumCylonLevel = cylonStream
                .map(user -> user.getPlayer().getSkillBook().get())
                .mapToLong(Short::longValue).sum();
        this.cylonSumLevel.set(currentSumCylonLevel);
        this.colonialCount.set(sizeOfFaction(Faction.Colonial));
        this.cylonCount.set(sizeOfFaction(Faction.Cylon));
    }

    public long sizeOfFaction(final Faction faction)
    {
        lockRead();
        try
        {
            return this.userMap.values().stream()
                    .filter(User::isConnected)
                    .map(User::getPlayer)
                    .filter(player -> player.getFaction() == faction)
                    .count();
        }
        finally
        {
            releaseRead();
        }
    }


    public User playerCreated(final Player player)
    {
        final long userID = player.getUserID();
        lockWrite();
        try
        {
            final Optional<PlayerlessUser> withoutChar = this.getWithoutCharInternal(userID);
            if (withoutChar.isPresent())
            {
                final PlayerlessUser associated = this.usersWithoutCharacter.remove(userID);
                User user = User.fromPlayerless(associated, player);
                try
                {
                    this.addInternal(user);
                }
                catch (NullPointerException nullPointerException)
                {
                    nullPointerException.printStackTrace();
                }
                log.info("Char createdfinished " + this.userMap.size() + " " + this.usersWithoutCharacter.size());

                return user;
            }
            throw new IllegalArgumentException("UserID could not find preChar " + userID);
        }
        finally
        {
            releaseWrite();
        }
    }

    public void remove(final User user)
    {
        lockWrite();
        try
        {
            this.userMap.remove(user.getPlayer().getUserID());
        }
        finally
        {
            releaseWrite();
        }
    }

    private void addInternal(final User newUser) throws NullPointerException
    {
        Objects.requireNonNull(newUser);
        Objects.requireNonNull(newUser.getPlayer());
        this.userMap.put(newUser.getPlayer().getUserID(), newUser);
    }
    public void addWithoutChar(final long id, final PlayerlessUser user)
    {
        lockWrite();
        try
        {
            this.usersWithoutCharacter.put(id, user);
        }
        finally
        {
            releaseWrite();
        }
    }

    /**
     * removes the user
     *
     * @param id
     * @return true if contained the user
     */
    public PlayerlessUser removeWithoutChar(final long id)
    {
        lockWrite();
        try
        {
            return this.usersWithoutCharacter.remove(id);
        }
        finally
        {
            releaseWrite();
        }
    }

    public Stream<User> userStream(final Predicate<User> predicate)
    {
        return this.userMap.values()
                .stream()
                .filter(predicate);
    }
    public Set<User> userSet(final Predicate<User> predicate)
    {
        lockRead();
        try
        {
            return this.userMap.values()
                    .stream()
                    .filter(predicate)
                    .collect(Collectors.toSet());
        }
        finally
        {
            releaseRead();
        }
    }
    public List<User> userList(final Predicate<User> predicate)
    {
        lockRead();
        try
        {
            return this.userMap.values()
                    .stream()
                    .filter(predicate)
                    .toList();
        }
        finally
        {
            releaseRead();
        }
    }
    public List<User> userList()
    {
        return new ArrayList<>(values());
    }

    public Collection<User> values()
    {
        return this.userMap.values();
    }
}
