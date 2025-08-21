package io.github.luigeneric.core.player.location;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;
import lombok.Getter;

public abstract class LocationState implements IProtocolWrite
{
    protected final Location location;
    @Getter
    protected final GameLocation gameLocation;
    protected final TransSceneType transSceneType;

    public LocationState(final Location location, final GameLocation gameLocation, TransSceneType transSceneType)
    {
        this.location = location;
        this.gameLocation = gameLocation;
        this.transSceneType = transSceneType;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.transSceneType.getValue());
        bw.writeByte(this.gameLocation.getValue());
    }

    public abstract void process(final BgoProtocolWriter bw);

    public void processLocationCommunitySubscriber(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.gameLocation.getValue());
    }

    public Location getLocation()
    {
        return this.location;
    }
}
