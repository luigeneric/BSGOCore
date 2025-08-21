package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;


import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.collidershapes.AABB;

public class BotTemplate extends SpaceObjectTemplate
{
    private final long respawnTimeJumpOut;
    private final boolean jumpOutIfInCombat;
    private final long lifeTimeSeconds;
    private final AABB spawnBox;

    public BotTemplate(long objectGUID, final SpaceEntityType spaceEntityType,
                       final CreatingCause creatingCause, final int respawnTime,
                       final boolean isInstantInSector,
                       final long[] lootTemplateIds, long respawnTimeJumpOut, boolean jumpOutIfInCombat, long lifeTimeSeconds,
                       final AABB spawnBox)
    {
        super(objectGUID, spaceEntityType, creatingCause, respawnTime, isInstantInSector, lootTemplateIds);
        this.respawnTimeJumpOut = respawnTimeJumpOut;
        this.jumpOutIfInCombat = jumpOutIfInCombat;
        this.lifeTimeSeconds = lifeTimeSeconds;
        this.spawnBox = spawnBox;
    }

    public long getRespawnTimeJumpOut()
    {
        return respawnTimeJumpOut;
    }

    public boolean isJumpOutIfInCombat()
    {
        return jumpOutIfInCombat;
    }

    public long getLifeTimeSeconds()
    {
        return lifeTimeSeconds;
    }

    public AABB getSpawnBox()
    {
        return spawnBox;
    }
}
