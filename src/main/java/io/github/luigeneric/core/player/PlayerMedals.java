package io.github.luigeneric.core.player;


import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;

public class PlayerMedals extends InfoPublisher<MedalStatus>
{
    public PlayerMedals(final long playerID, final MedalStatus initialMedals)
    {
        super(InfoType.Medal, playerID, initialMedals);
    }
}
