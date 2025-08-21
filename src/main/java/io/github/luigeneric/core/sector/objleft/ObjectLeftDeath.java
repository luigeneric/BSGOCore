package io.github.luigeneric.core.sector.objleft;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;

import java.util.Optional;

public class ObjectLeftDeath extends ObjectLeftDescription
{
    private final SpaceObject killerObject;
    public ObjectLeftDeath(final SpaceObject removedSpaceObject, final SpaceObject killerObject)
    {
        super(removedSpaceObject, RemovingCause.Death);
        this.killerObject = killerObject;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(0); //next read but not used value
    }

    public Optional<SpaceObject> getKillerObject()
    {
        return Optional.ofNullable(this.killerObject);
    }
}
