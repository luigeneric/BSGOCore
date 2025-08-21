package io.github.luigeneric.core.player.location;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;

public class SpaceLocation extends LocationState
{
    public SpaceLocation(Location location)
    {
        super(location, GameLocation.Space, TransSceneType.Undock);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeUInt32(location.getSectorID());
        bw.writeGUID(location.getSectorGUID());
    }

    @Override
    public void process(BgoProtocolWriter bw)
    {
        this.write(bw);
    }

    @Override
    public void processLocationCommunitySubscriber(BgoProtocolWriter bw)
    {
        super.processLocationCommunitySubscriber(bw);
        bw.writeGUID(location.getSectorGUID());
    }
}

