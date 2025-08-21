package io.github.luigeneric.core.player.counters;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.MissionUpdater;
import io.github.luigeneric.utils.collections.SmartMap;

import java.time.LocalDateTime;
import java.util.*;

public class MissionBook extends SmartMap<Mission> implements IProtocolWrite
{
    private final long userId;
    private LocalDateTime lastTimeMissionsRequested;
    private final Set<Integer> requiredUpdateIds;
    private final MissionUpdater missionUpdater;

    private MissionBook(final long userId, final Map<Integer, Mission> items, final LocalDateTime lastTimeMissionsRequested, final MissionUpdater missionUpdater)
    {
        super(items);
        this.userId = userId;
        this.lastTimeMissionsRequested = lastTimeMissionsRequested;
        this.requiredUpdateIds = new HashSet<>();
        this.missionUpdater = missionUpdater;
    }
    public MissionBook(final long userId, final MissionUpdater missionUpdater)
    {
        this(userId, new HashMap<>(), LocalDateTime.MIN, missionUpdater);
    }

    public void inject(final Mission mission)
    {
        writeLock.lock();
        try
        {
            this.items.put(mission.getServerID(), mission);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    @Override
    public void addItem(Mission newItem)
    {
        writeLock.lock();
        try
        {
            this.items.put(newItem.getServerID(), newItem);
            this.requiredUpdateIds.add(newItem.getServerID());
            missionUpdater.updateRequired(userId);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void incrementCountByForAll(final long counterCardGuid, final long sectorCardGuid, final double byValue)
    {
        writeLock.lock();
        try
        {
            boolean oneUpdated = false;
            for (final Mission mission : this.items.values())
            {
                final boolean updated = mission.incrementCountBy(counterCardGuid, sectorCardGuid, byValue);
                if (updated)
                {
                    this.requiredUpdateIds.add(mission.getServerID());
                    oneUpdated = true;
                }

            }
            if (oneUpdated)
                missionUpdater.updateRequired(userId);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void setLastTimeMissionsRequested(final LocalDateTime lastTimeMissionsRequested)
    {
        Objects.requireNonNull(lastTimeMissionsRequested);

        this.lastTimeMissionsRequested = lastTimeMissionsRequested;
    }

    public LocalDateTime getLastTimeMissionsRequested()
    {
        return lastTimeMissionsRequested;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        readLock.lock();
        try
        {
            bw.writeLength(requiredUpdateIds.size());
            for (final Mission value : this.items.values())
            {
                if(requiredUpdateIds.contains(value.getServerID()))
                {
                    bw.writeDesc(value);
                }
            }
            requiredUpdateIds.clear();
        }
        finally
        {
            readLock.unlock();
        }
    }
    public void initAllUpdate()
    {
        writeLock.lock();
        try
        {
            this.items.values().forEach(mission -> this.requiredUpdateIds.add(mission.getServerID()));
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public boolean requiresUpdate()
    {
        readLock.lock();
        try
        {
            return !this.requiredUpdateIds.isEmpty();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        return "MissionBook{" +
                "lastTimeMissionsRequested=" + lastTimeMissionsRequested +
                ", requiredUpdateIds=" + requiredUpdateIds +
                ", items=" + items +
                '}';
    }

    public void reset()
    {
        writeLock.lock();
        try
        {
            this.items.clear();
            this.lastTimeMissionsRequested = LocalDateTime.MIN;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void resetWithoutTimestamp()
    {
        writeLock.lock();
        try
        {
            this.items.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }
}
