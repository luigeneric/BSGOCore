package io.github.luigeneric.utils.publishersubscriber;

public interface Subscriber<T>
{
    void onUpdate(final T arg);
}
