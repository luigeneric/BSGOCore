package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class StaticNpcTemplate extends SpaceObjectTemplate
{
    protected final Vector3 position;
    protected final Euler3 rotation;
    protected final float autoAggroDistance;
    protected final float maximumAggroDistance;
    protected final Faction faction;
    protected StaticNpcTemplate(long objectGUID, SpaceEntityType spaceEntityType, CreatingCause creatingCause, final int respawnTime,
                                final boolean isInstantInSector, final Vector3 position, final Euler3 rotation,
                                final float autoAggroDistance, float maximumAggroDistance, Faction faction, final long[] lootTemplateIds)
    {
        super(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, lootTemplateIds);
        this.position = position;
        this.rotation = rotation;
        this.autoAggroDistance = autoAggroDistance;
        this.maximumAggroDistance = maximumAggroDistance;
        this.faction = faction;
    }

    public final float getAutoAggroDistance()
    {
        return autoAggroDistance;
    }

    public final float getMaximumAggroDistance()
    {
        return maximumAggroDistance;
    }

    public final Faction getFaction()
    {
        return faction;
    }

    public Transform getTransform()
    {
        return new Transform(position, rotation, true);
    }
}
