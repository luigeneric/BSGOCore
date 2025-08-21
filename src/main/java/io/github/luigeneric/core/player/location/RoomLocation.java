package io.github.luigeneric.core.player.location;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;

public abstract class RoomLocation extends LocationState
{
    public RoomLocation(final Location location, final TransSceneType transSceneType)
    {
        super(location, GameLocation.Room, transSceneType);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeGUID(this.getRoomGUID());
        bw.writeUInt32(this.location.getSectorID());
    }

    @Override
    public void process(BgoProtocolWriter bw)
    {
        this.write(bw);
    }

    public abstract long getRoomGUID();

    @Override
    public void processLocationCommunitySubscriber(BgoProtocolWriter bw)
    {
        super.processLocationCommunitySubscriber(bw);
        bw.writeGUID(location.getSectorGUID());
        bw.writeGUID(location.getRoomGUID());
    }
}
