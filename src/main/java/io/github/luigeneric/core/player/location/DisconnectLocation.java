package io.github.luigeneric.core.player.location;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;

public class DisconnectLocation extends LocationState
{
    public DisconnectLocation(Location location, TransSceneType previousSceneType)
    {
        super(location, GameLocation.Disconnect, previousSceneType);
    }

    @Override
    public void process(BgoProtocolWriter bw)
    {

    }
}
