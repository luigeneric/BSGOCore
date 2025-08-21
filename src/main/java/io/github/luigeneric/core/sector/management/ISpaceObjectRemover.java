package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;

public interface ISpaceObjectRemover
{
    void notifyRemovingCauseAdded(final SpaceObject spaceObject, final RemovingCause removingCause, final SpaceObject removingCauseObject);
    void notifyRemovingCauseAdded(final SpaceObject spaceObject, final RemovingCause removingCause);

    void playerSelectedRespawnLocation(final long userID);
    void notifyUserDisconnected(final long userId);
}
