package io.github.luigeneric.core.player;


import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;

public class PlayerGuild extends InfoPublisher<Guild>
{
    public PlayerGuild(final long playerID, final Guild initialInfo)
    {
        super(InfoType.Wing, playerID, initialInfo);
    }
}
