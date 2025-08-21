package io.github.luigeneric.core.player;


import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;

public class PlayerName extends InfoPublisher<String>
{
    public PlayerName(final long playerID, final String initialName)
    {
        super(InfoType.Name, playerID, initialName);
    }

    public boolean hasName()
    {
        return !this.currentInfo.equals("");
    }

    @Override
    public String toString()
    {
        return this.currentInfo;
    }
}
