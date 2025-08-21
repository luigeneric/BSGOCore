package io.github.luigeneric.utils.collections;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * The Key is always an unsigned short
 * @param <T>
 */
public abstract class SmartMap<T extends IServerItem>
{
    protected final Map<Integer, T> items;
    protected final Lock readLock;
    protected final Lock writeLock;
    public SmartMap(final Map<Integer, T> items) throws NullPointerException
    {
        this.items = Objects.requireNonNull(items, "Items-Map cannot be null");
        final ReadWriteLock tmpAll = new ReentrantReadWriteLock();
        this.readLock = tmpAll.readLock();
        this.writeLock = tmpAll.writeLock();
    }

    private int getFreeServerID() throws IllegalStateException
    {
        for (int i = 0; i < (Short.MAX_VALUE * 2 + 1); i++)
        {
            if (!this.items.containsKey(i))
            {
                return i;
            }
        }
        throw new IllegalStateException("Can not add, no free ID found");
    }

    public void addItem(final T newItem)
    {
        writeLock.lock();
        try
        {
            final int freeID = this.getFreeServerID();
            newItem.setServerID(freeID);
            this.items.put(freeID, newItem);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    public T removeItem(final int serverID)
    {
        writeLock.lock();
        try
        {
            return this.items.remove(serverID);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public int size()
    {
        readLock.lock();
        try
        {
            return this.items.size();
        }
        finally
        {
            readLock.unlock();
        }
    }

    public Optional<T> getByID(final int serverID)
    {
        readLock.lock();
        try
        {
            return Optional.ofNullable(this.items.get(serverID));
        }
        finally
        {
            readLock.unlock();
        }
    }
    public Optional<T> find(final Predicate<T> predicate)
    {
        readLock.lock();
        try
        {
            return this.items.values().stream().filter(predicate).findAny();
        }
        finally
        {
            readLock.unlock();
        }
    }
    public List<T> findAll(final Predicate<T> predicate)
    {
        readLock.lock();
        try
        {
            return this.items.values().stream().filter(predicate).toList();
        }
        finally
        {
            readLock.unlock();
        }
    }
    public boolean contains(final int serverID)
    {
        readLock.lock();
        try
        {
            return this.items.containsKey(serverID);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public Map<Integer, T> getItems()
    {
        return Collections.unmodifiableMap(items);
    }
}
