package io.github.luigeneric.core.sector.management.damage;



import io.github.luigeneric.core.sector.management.ObjectLeftSubscriber;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SectorDamageHistory implements ObjectLeftSubscriber
{
    /**
     * SpaceObject-ID, DamageHistory of the Object
     */
    private final Map<Long, ObjectDamageHistory> objectDamageHistoryMap;
    private final SectorUsers sectorUsers;

    public SectorDamageHistory(final Map<Long, ObjectDamageHistory> objectDamageHistoryMap, final SectorUsers sectorUsers)
    {
        this.objectDamageHistoryMap = objectDamageHistoryMap;
        this.sectorUsers = sectorUsers;
    }
    public SectorDamageHistory(final SectorUsers sectorUsers)
    {
        this(new HashMap<>(), sectorUsers);
    }

    public void damageUpdate(final DamageRecord damageRecord)
    {
        ObjectDamageHistory dmgHistory = this.objectDamageHistoryMap.get(damageRecord.to().getObjectID());
        if (dmgHistory == null)
        {
            dmgHistory = new ObjectDamageHistory(sectorUsers);
            this.objectDamageHistoryMap.put(damageRecord.to().getObjectID(), dmgHistory);
        }
        dmgHistory.damageReceived(damageRecord);
    }

    public ObjectDamageHistory getDamageHistory(final SpaceObject spaceObject)
    {
        return this.getDamageHistory(spaceObject.getObjectID());
    }
    public Optional<AccumulatedDamage> getKillShotDealerOfObject(final SpaceObject spaceObject)
    {
        final ObjectDamageHistory dmgHistory = getDamageHistory(spaceObject);
        if (dmgHistory == null)
            return Optional.empty();

        return dmgHistory.getKillShotDealer();
    }
    public ObjectDamageHistory getDamageHistory(final long objectID)
    {
        return this.objectDamageHistoryMap.get(objectID);
    }


    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        this.objectDamageHistoryMap.remove(arg.getRemovedSpaceObject().getObjectID());
    }
}

