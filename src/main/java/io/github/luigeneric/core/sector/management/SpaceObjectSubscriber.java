package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.spaceentities.SpaceObject;

public interface SpaceObjectSubscriber
{
    void objectAdded(final SpaceObject spaceObject);
}
