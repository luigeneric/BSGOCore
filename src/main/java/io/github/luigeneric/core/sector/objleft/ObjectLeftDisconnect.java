package io.github.luigeneric.core.sector.objleft;


import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;

public class ObjectLeftDisconnect extends ObjectLeftDescription
{
    public ObjectLeftDisconnect(final SpaceObject removedSpaceObject)
    {
        super(removedSpaceObject, RemovingCause.Disconnection);
    }
}
