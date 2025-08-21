package io.github.luigeneric.core.player;


import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;

public class PlayerAvatar extends InfoPublisher<AvatarDescription>
{
    public PlayerAvatar(final long playerID, final AvatarDescription initialAvatar)
    {
        super(InfoType.Avatar, playerID, initialAvatar);
    }
}
