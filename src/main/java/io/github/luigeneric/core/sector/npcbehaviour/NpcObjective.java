package io.github.luigeneric.core.sector.npcbehaviour;

public abstract class NpcObjective
{
    protected final NpcObjectiveType type;
    protected final int priority;

    protected NpcObjective(final NpcObjectiveType type, final int priority)
    {
        this.type = type;
        this.priority = priority;
    }

    public boolean priorityIsHigherThan(final NpcObjective other)
    {
        return this.priority < other.priority;
    }

    public NpcObjectiveType getType()
    {
        return type;
    }

    public int getPriority()
    {
        return priority;
    }
}

