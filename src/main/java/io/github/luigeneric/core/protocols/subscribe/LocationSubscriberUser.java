package io.github.luigeneric.core.protocols.subscribe;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.location.LocationState;
import io.github.luigeneric.core.player.subscribesystem.UserInfoTypeSubscriber;

public class LocationSubscriberUser extends UserInfoTypeSubscriber<LocationState>
{
    public LocationSubscriberUser(User user, SubscribeProtocolWriteOnly subscribeProtocol)
    {
        super(user, subscribeProtocol);
    }

    @Override
    public void onUpdate(long playerID, InfoType infoType, LocationState location)
    {
        user.send(subscribeProtocolWriteOnly.writePlayerLocation(playerID, location.getLocation()));
    }
}
