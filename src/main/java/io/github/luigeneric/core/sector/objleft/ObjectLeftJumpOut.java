package io.github.luigeneric.core.sector.objleft;


import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;

public class ObjectLeftJumpOut extends ObjectLeftDescription
{
    public ObjectLeftJumpOut(final SpaceObject removedSpaceObject)
    {
        super(removedSpaceObject, RemovingCause.JumpOut);
    }
}
