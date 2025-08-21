package io.github.luigeneric.core.sector.npcbehaviour;


import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.List;

public class KillObjective extends NpcObjective
{
    private final List<SpaceObject> objectivesToKill;

    public KillObjective(final int priority, final List<SpaceObject> objectivesToKill)
    {
        super(NpcObjectiveType.Kill, priority);
        this.objectivesToKill = objectivesToKill;
    }

    public List<SpaceObject> getObjectivesToKill()
    {
        return objectivesToKill;
    }
}
