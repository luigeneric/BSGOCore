package io.github.luigeneric.core.player.location;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;

public class AvatarLocation extends LocationState
{
    public AvatarLocation(Location location)
    {
        super(location, GameLocation.Avatar, TransSceneType.FirstStory);
    }

    @Override
    public void process(BgoProtocolWriter bw)
    {
        this.write(bw);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);

        //nothing important here, just dead code on client side
        bw.writeLength(0);
        bw.writeBoolean(false);
    }
}
