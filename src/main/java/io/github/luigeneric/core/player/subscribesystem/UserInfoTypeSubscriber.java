package io.github.luigeneric.core.player.subscribesystem;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.protocols.subscribe.SubscribeProtocolWriteOnly;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class UserInfoTypeSubscriber<T> implements InfoTypeSubscriber<T>
{
    protected final User user;
    protected final SubscribeProtocolWriteOnly subscribeProtocolWriteOnly;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfoTypeSubscriber<?> that = (UserInfoTypeSubscriber<?>) o;

        return user.getPlayer().equals(that.user.getPlayer());
    }

    @Override
    public int hashCode()
    {
        return user != null ? user.getPlayer().hashCode() : 0;
    }
}
