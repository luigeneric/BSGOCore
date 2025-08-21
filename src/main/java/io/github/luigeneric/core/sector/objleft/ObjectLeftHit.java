package io.github.luigeneric.core.sector.objleft;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;

public class ObjectLeftHit extends ObjectLeftDescription
{
    private final long targetHitObjectId;

    public ObjectLeftHit(final SpaceObject removedSpaceObject, final SpaceObject targetHit)
    {
        super(removedSpaceObject, RemovingCause.Hit);
        this.targetHitObjectId = targetHit.getObjectID();
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.targetHitObjectId);
    }

}
