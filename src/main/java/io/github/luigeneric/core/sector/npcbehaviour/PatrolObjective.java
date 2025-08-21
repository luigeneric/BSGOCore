package io.github.luigeneric.core.sector.npcbehaviour;


import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.AABB;

public class PatrolObjective extends NpcObjective
{
    private final AABB boxToPatrolIn;

    public PatrolObjective(final int priority, final AABB boxToPatrolIn)
    {
        super(NpcObjectiveType.Patrol, priority);
        this.boxToPatrolIn = boxToPatrolIn;
    }

    public boolean isInsideBox(final Vector3 myPosition)
    {
        return boxToPatrolIn.isVectorInBounds(myPosition);
    }
    public Euler3 getDirectionToCenter(final Vector3 from)
    {
        return Euler3.direction(Vector3.sub(this.boxToPatrolIn.center(), from));
    }

    public AABB getBoxToPatrolIn()
    {
        return boxToPatrolIn;
    }
}
