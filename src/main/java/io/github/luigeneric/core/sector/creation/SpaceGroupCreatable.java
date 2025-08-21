package io.github.luigeneric.core.sector.creation;


import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.List;


public interface SpaceGroupCreatable
{
    SpaceObject getParent() throws IllegalAccessException;
    List<SpaceObject> getChildren();
    void create();
}