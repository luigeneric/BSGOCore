package io.github.luigeneric.core.player.subscribesystem;

import io.github.luigeneric.core.protocols.subscribe.InfoType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InfoPublisher<T> implements IInfoTypePublisher<T>
{
    protected final Map<Long, UserInfoTypeSubscriber<T>> subscribers;
    protected final InfoType infoType;
    protected final long playerID;
    protected T currentInfo;

    public InfoPublisher(final Map<Long, UserInfoTypeSubscriber<T>> subscribers, final InfoType infoType, final long playerID, final T initialInfo)
    {
        this.subscribers = subscribers;
        this.infoType = Objects.requireNonNull(infoType, "InfoType cannot be null!");
        this.playerID = playerID;
        this.set(initialInfo);
    }
    public InfoPublisher(final InfoType infoType, final long playerID, final T initialInfo)
    {
        this(new ConcurrentHashMap<>(), infoType, playerID, initialInfo);
    }

    @Override
    public void set(final T currentInfo)
    {
        this.currentInfo = currentInfo;
        this.updateSubscribers(playerID, infoType, currentInfo);
    }

    @Override
    public void updateSubscribers(final long playerID, final InfoType infoType, final T value)
    {
        for (final UserInfoTypeSubscriber<T> subscriber : this.subscribers.values())
        {
            subscriber.onUpdate(playerID, infoType, value);
        }
    }

    @Override
    public T get()
    {
        return this.currentInfo;
    }


    @Override
    public void addSubscriber(final UserInfoTypeSubscriber<T> subscriber)
    {
        this.subscribers.put(subscriber.user.getPlayer().getUserID(), subscriber);
        if (this.currentInfo != null)
            subscriber.onUpdate(this.playerID, this.infoType, this.get());
    }

    @Override
    public void removeSubscriber(final long userID)
    {
        this.subscribers.remove(userID);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InfoPublisher<?> that = (InfoPublisher<?>) o;

        if (playerID != that.playerID) return false;
        return infoType == that.infoType;
    }

    @Override
    public int hashCode()
    {
        int result = infoType.hashCode();
        result = 31 * result + (int) (playerID ^ (playerID >>> 32));
        return result;
    }
}
