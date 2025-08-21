package io.github.luigeneric.core.player.subscribesystem;


import io.github.luigeneric.core.protocols.subscribe.InfoType;

public interface IInfoTypePublisher<T>
{
    T get();
    void set(final T currentInfo);
    void updateSubscribers(final long playerID, final InfoType infoType, final T value);
    void addSubscriber(final UserInfoTypeSubscriber<T> subscriber);
    void removeSubscriber(final long subscriber);
}
