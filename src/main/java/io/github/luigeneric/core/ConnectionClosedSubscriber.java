package io.github.luigeneric.core;

public interface ConnectionClosedSubscriber
{
    void onConnectionClosed(final AbstractConnection abstractConnection, final String reason);
}
