package io.github.luigeneric.core.player;


import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;
import io.github.luigeneric.enums.Faction;

public class PlayerFaction extends InfoPublisher<Faction>
{
    public PlayerFaction(final long playerID, final Faction initialFaction)
    {
        super(InfoType.Faction, playerID, initialFaction);
    }
}
