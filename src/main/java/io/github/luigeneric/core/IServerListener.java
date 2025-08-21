package io.github.luigeneric.core;

public interface IServerListener extends Runnable
{
    boolean isShutdown();
    void shutdown();

    void setServerListenerSubscriber(IServerListenerSubscriber serverListenerSubscriber);
}
