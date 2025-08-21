package io.github.luigeneric.core.sector.objleft;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;

public class ObjectLeftCollected extends ObjectLeftDescription
{
    private final int secondsCollect;

    public ObjectLeftCollected(final SpaceObject removedSpaceObject, final int secondsCollect)
    {
        super(removedSpaceObject, RemovingCause.Collected);
        this.secondsCollect = secondsCollect;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.secondsCollect);
    }
}
