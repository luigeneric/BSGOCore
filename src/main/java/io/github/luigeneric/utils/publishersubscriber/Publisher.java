package io.github.luigeneric.utils.publishersubscriber;

public interface Publisher<T>
{
    void updateSubscribers();
    void addSubscriber(final Subscriber<T> subscriber);
    void removeSubscriber(final Subscriber<T> subscriber);
}
