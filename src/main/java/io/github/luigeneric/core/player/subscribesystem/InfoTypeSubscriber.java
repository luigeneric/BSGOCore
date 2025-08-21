package io.github.luigeneric.core.player.subscribesystem;


import io.github.luigeneric.core.protocols.subscribe.InfoType;

public interface InfoTypeSubscriber<T>
{
    void onUpdate(final long playerID, final InfoType infoType, final T t);
}
