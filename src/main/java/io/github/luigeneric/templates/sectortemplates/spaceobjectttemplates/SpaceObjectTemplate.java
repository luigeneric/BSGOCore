package io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates;



import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.SpaceEntityType;

import java.util.Arrays;

public abstract class SpaceObjectTemplate
{
    protected final long objectGUID;
    protected final SpaceEntityType spaceEntityType;
    protected final CreatingCause creatingCause;
    //in seconds if 0 it has no respawn
    protected int respawnTime;
    protected final boolean isInstantInSector;
    protected final long[] lootTemplateIds;

    protected SpaceObjectTemplate(long objectGUID, SpaceEntityType spaceEntityType, CreatingCause creatingCause,
                                  int respawnTime, boolean isInstantInSector, final long[] lootTemplateIds)
    {
        this.objectGUID = objectGUID;
        this.spaceEntityType = spaceEntityType;
        this.creatingCause = creatingCause;
        this.respawnTime = respawnTime;
        this.isInstantInSector = isInstantInSector;
        this.lootTemplateIds = lootTemplateIds;
    }

    public long getObjectGUID()
    {
        return objectGUID;
    }

    public SpaceEntityType getSpaceEntityType()
    {
        return spaceEntityType;
    }

    public CreatingCause getCreatingCause()
    {
        return creatingCause;
    }

    public int getRespawnTime()
    {
        return respawnTime;
    }

    public void setRespawnTime(final int respawnSeconds)
    {
        this.respawnTime = respawnSeconds;
    }

    public boolean isInstantInSector()
    {
        return isInstantInSector;
    }

    public long[] getLootTemplateIds()
    {
        return lootTemplateIds;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpaceObjectTemplate that = (SpaceObjectTemplate) o;

        if (objectGUID != that.objectGUID) return false;
        if (respawnTime != that.respawnTime) return false;
        if (isInstantInSector != that.isInstantInSector) return false;
        if (spaceEntityType != that.spaceEntityType) return false;
        if (creatingCause != that.creatingCause) return false;
        return Arrays.equals(lootTemplateIds, that.lootTemplateIds);
    }

    @Override
    public int hashCode()
    {
        int result = (int) (objectGUID ^ (objectGUID >>> 32));
        result = 31 * result + (spaceEntityType != null ? spaceEntityType.hashCode() : 0);
        result = 31 * result + (creatingCause != null ? creatingCause.hashCode() : 0);
        result = 31 * result + respawnTime;
        result = 31 * result + (isInstantInSector ? 1 : 0);
        result = 31 * result + Arrays.hashCode(lootTemplateIds);
        return result;
    }
}
