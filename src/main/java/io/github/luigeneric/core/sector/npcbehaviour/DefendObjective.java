package io.github.luigeneric.core.sector.npcbehaviour;


import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.List;

public class DefendObjective extends NpcObjective
{
    private final List<SpaceObject> objectivesToDefend;

    public DefendObjective(final int priority, final List<SpaceObject> objectivesToDefend)
    {
        super(NpcObjectiveType.Defend, priority);
        this.objectivesToDefend = objectivesToDefend;
    }

    public List<SpaceObject> getObjectivesToDefend()
    {
        return objectivesToDefend;
    }
}
