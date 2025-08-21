package io.github.luigeneric.core.protocols.subscribe;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.HangarShipsUpdate;
import io.github.luigeneric.core.player.subscribesystem.UserInfoTypeSubscriber;

public class ShipsSubscriberUser extends UserInfoTypeSubscriber<HangarShipsUpdate>
{
    public ShipsSubscriberUser(User user, SubscribeProtocolWriteOnly subscribeProtocol)
    {
        super(user, subscribeProtocol);
    }

    @Override
    public void onUpdate(long playerID, InfoType infoType, HangarShipsUpdate hangarShipsUpdate)
    {
        user.send(subscribeProtocolWriteOnly.writePlayerShips(playerID, hangarShipsUpdate));
    }
}
