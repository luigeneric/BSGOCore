package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ObjectIDRegistry implements ObjectLeftSubscriber
{
    private final Set<Long> overallUsedIds;
    private final Map<SpaceEntityType, RingCounter> idCounterMap;
    private final Lock lock;
    public ObjectIDRegistry(final Set<Long> overallUsedIds, Map<SpaceEntityType, RingCounter> idCounterMap, final Lock lock)
    {
        this.overallUsedIds = overallUsedIds;
        this.idCounterMap = idCounterMap;
        this.lock = lock;
    }

    public ObjectIDRegistry()
    {
        this(new HashSet<>(), new HashMap<>(), new ReentrantLock());
    }

    public long getFreeObjectId(final SpaceEntityType spaceEntityType, final Faction faction)
    {
        return this.getFreeObjectId(spaceEntityType, faction, FactionGroup.Group0);
    }
    public long getFreeObjectId(final SpaceEntityType spaceEntityType, final Faction faction, final FactionGroup factionGroup)
    {
        lock.lock();
        try
        {
            final RingCounter ringCounter = this.idCounterMap
                    .getOrDefault(spaceEntityType, new RingCounter(spaceEntityType.value, spaceEntityType.value+0x01000000L));

            long freeID = ringCounter.getAndIncrementID(faction, factionGroup);
            while (!this.overallUsedIds.add(freeID))
            {
                freeID = ringCounter.getAndIncrementID(faction, factionGroup);
            }

            this.idCounterMap.put(spaceEntityType, ringCounter);

            return freeID;
        }
        finally
        {
            lock.unlock();
        }
    }
    public boolean removeObjectId(final long id)
    {
        lock.lock();
        try
        {
            return this.overallUsedIds.remove(id);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        final boolean contained = this.removeObjectId(arg.getRemovedSpaceObject().getObjectID());
        if (!contained)
        {
            log.error("SpaceObject removed id but did not contain! " + arg);
        }
    }
}
