package io.github.luigeneric.core.protocols.subscribe;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.MedalStatus;
import io.github.luigeneric.core.player.subscribesystem.UserInfoTypeSubscriber;

public class MedalSubscriberUser extends UserInfoTypeSubscriber<MedalStatus>
{
    public MedalSubscriberUser(User user, SubscribeProtocolWriteOnly subscribeProtocol)
    {
        super(user, subscribeProtocol);
    }

    @Override
    public void onUpdate(long playerID, InfoType infoType, MedalStatus medalStatus)
    {
        user.send(subscribeProtocolWriteOnly.writePlayerMedal(playerID, medalStatus));
    }
}
