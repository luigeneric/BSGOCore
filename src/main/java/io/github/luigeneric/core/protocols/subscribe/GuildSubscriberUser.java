package io.github.luigeneric.core.protocols.subscribe;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.player.subscribesystem.UserInfoTypeSubscriber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuildSubscriberUser extends UserInfoTypeSubscriber<Guild>
{
    public GuildSubscriberUser(User user, SubscribeProtocolWriteOnly subscribeProtocolWriteOnly)
    {
        super(user, subscribeProtocolWriteOnly);
    }

    @Override
    public void onUpdate(final long playerID, final InfoType infoType, final Guild guild)
    {
        if (guild != null)
        {
            try
            {
                user.send(subscribeProtocolWriteOnly.writePlayerGuild(playerID, guild));
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                log.error(user.getUserLog() + "IllegalArgument in guildSubscriber occured " + illegalArgumentException.getMessage());
            }
        }
    }
}
