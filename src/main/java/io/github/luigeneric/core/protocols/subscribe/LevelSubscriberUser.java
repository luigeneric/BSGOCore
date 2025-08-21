package io.github.luigeneric.core.protocols.subscribe;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.subscribesystem.UserInfoTypeSubscriber;

public class LevelSubscriberUser extends UserInfoTypeSubscriber<Short>
{
    public LevelSubscriberUser(User user, SubscribeProtocolWriteOnly subscribeProtocol)
    {
        super(user, subscribeProtocol);
    }

    @Override
    public void onUpdate(long playerID, InfoType infoType, Short aShort)
    {
        user.send(subscribeProtocolWriteOnly.writePlayerLevel(playerID, aShort));
    }
}
