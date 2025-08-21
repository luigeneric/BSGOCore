package io.github.luigeneric.core.protocols;


import io.github.luigeneric.core.User;

import java.util.Collection;
import java.util.NoSuchElementException;

public interface IProtocolRegistry
{
    <T extends BgoProtocol> T getProtocol(final ProtocolID protocolID) throws NoSuchElementException;
    void loginFinished(final User user);
    Collection<BgoProtocol> getAllProtocols();

    void injectOldRegistry(final User user);

    void onDisconnect();
}
