package io.github.luigeneric.core.community.guild;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ApplicationScoped
public class GuildRegistry implements GuildSubscriber
{
    private final Map<Long, Guild> guilds;
    private final Set<GuildInvitedEntry> guildSendToHistory;
    private final ReadWriteLock readWriteLock;

    public GuildRegistry()
    {
        this.guilds = new HashMap<>();
        this.guildSendToHistory = new CopyOnWriteArraySet<>();
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

    private long getNextFreeID()
    {
        for (long i = 1; i < (Integer.MAX_VALUE * 2L + 1); i++)
        {
            if (!this.guilds.containsKey(i))
            {
                return i;
            }
        }
        throw new IllegalStateException("Can not add, no free ID found");
    }

    public Guild createGuildIfNotExists(final String name) throws IllegalArgumentException
    {
        lockWrite();
        try
        {
            if (this._checkGuildExists(name))
            {
                throw new IllegalArgumentException("Guild already exists");
            }

            final long nextFreeID = this.getNextFreeID();
            final Guild guild = new Guild(nextFreeID, name, this);
            this.guilds.put(nextFreeID, guild);
            return guild;
        }
        finally
        {
            releaseWrite();
        }
    }
    private void removeGuild(final long id)
    {
        lockWrite();
        try
        {
            guilds.remove(id);
        }
        finally
        {
            releaseWrite();
        }
    }
    public Guild createExistingGuild(final String name, final long guildId)
    {
        lockWrite();
        try
        {
            final Guild guild = new Guild(guildId, name, this);
            this.guilds.put(guildId, guild);
            return guild;
        }
        finally
        {
            releaseWrite();
        }
    }

    public List<Guild> getAllGuilds()
    {
        lockRead();
        try
        {
            return new ArrayList<>(guilds.values());
        }
        finally
        {
            releaseRead();
        }
    }
    public Optional<Guild> getGuildOfPlayerID(final long playerID)
    {
        lockRead();
        try
        {
            for (final Guild guild : this.guilds.values())
            {
                final Optional<GuildMemberInfo> optMemberInfo = guild.getGuildMemberInfoOf(playerID);
                if (optMemberInfo.isPresent())
                    return Optional.of(guild);
            }
            return Optional.empty();
        }
        finally
        {
            releaseRead();
        }
    }

    private boolean _checkGuildExists(final String name)
    {
        return this.guilds.values().stream().anyMatch(pred -> pred.getName().equals(name));
    }

    public Optional<Guild> getGuild(final long guildID)
    {
        lockRead();
        try
        {
            return Optional.ofNullable(this.guilds.get(guildID));
        }
        finally
        {
            releaseRead();
        }
    }

    public Set<GuildInvitedEntry> getGuildSendToHistory()
    {
        return guildSendToHistory;
    }

    @Override
    public void notifyMemberCountZero(long guildId)
    {
        removeGuild(guildId);
    }
}
