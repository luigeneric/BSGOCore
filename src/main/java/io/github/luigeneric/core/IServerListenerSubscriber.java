package io.github.luigeneric.core;

public interface IServerListenerSubscriber
{
    void notifyNewConnection(final AbstractConnection newConnection);
}
