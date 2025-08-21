package io.github.luigeneric.core.player.location;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.TransSceneType;

public class ZoneLocation extends LocationState
{
    public ZoneLocation(Location location)
    {
        super(location, GameLocation.Zone, TransSceneType.Tournament);
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(location.getSectorID());
        bw.writeGUID(location.getSectorGUID());
        bw.writeGUID(location.getZoneGUID());
    }

    @Override
    public void process(BgoProtocolWriter bw)
    {
        this.write(bw);
    }
}
