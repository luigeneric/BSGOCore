package io.github.luigeneric.core.player.counters;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.utils.publishersubscriber.Publisher;
import io.github.luigeneric.utils.publishersubscriber.Subscriber;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CounterDesc implements IProtocolWrite, Publisher<CounterDesc>
{
    private final long guid;
    private double value;
    private final Lock readLock;
    private final Lock writeLock;
    private final Set<Subscriber<CounterDesc>> subscribers;

    public CounterDesc(final long guid, final double initialValue)
    {
        this.guid = guid;
        this.value = initialValue;
        final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.subscribers = new CopyOnWriteArraySet<>();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeGUID(guid);

        readLock.lock();
        try
        {
            bw.writeInt32((int)value);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public long getGuid()
    {
        return guid;
    }

    private void internalSetValue(final double newValue)
    {
        this.value = newValue;
        this.updateSubscribers();
    }

    protected void setValue(final double newValue)
    {
        writeLock.lock();
        try
        {
            this.internalSetValue(newValue);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    protected void addValue(final double incrementBy)
    {
        writeLock.lock();
        try
        {
            final double newValue = this.value + incrementBy;
            this.internalSetValue(newValue);
        }
        finally
        {
            writeLock.unlock();
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CounterDesc that = (CounterDesc) o;

        return guid == that.guid;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(guid);
    }

    public long getLongValue()
    {
        return (long) getValue();
    }
    public double getValue()
    {
        readLock.lock();
        try
        {
            return this.value;
        }
        finally
        {
            readLock.unlock();
        }
    }


    @Override
    public String toString()
    {
        return "CounterDesc{" +
                "guid=" + guid +
                ", value=" + value +
                '}';
    }

    @Override
    public void updateSubscribers()
    {
        for (final Subscriber<CounterDesc> subscriber : this.subscribers)
        {
            subscriber.onUpdate(this);
        }
    }

    @Override
    public void addSubscriber(final Subscriber<CounterDesc> subscriber)
    {
        this.subscribers.add(subscriber);
        subscriber.onUpdate(this);
    }

    @Override
    public void removeSubscriber(final Subscriber<CounterDesc> subscriber)
    {
        this.subscribers.remove(subscriber);
    }
}
