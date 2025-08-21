package io.github.luigeneric.core.player.counters;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.utils.collections.IServerItem;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Mission implements IProtocolWrite, IServerItem
{
    /**
     * id unique only to the MissionBook bound to 1 player
     */
    private int serverID;
    /**
     * For GUICard and MissionCard
     */
    private final long missionCardGUID;
    /**
     * If 0, no sector associated
     */
    private final long associatedSectorCardGUID;
    /**
     * Multiple countables based on counters
     */
    private final Map<Long, MissionCountable> missionCountables;
    /**
     * May be InProgress or Completed
     */
    private MissionState missionState;
    private final Lock lock;

    public Mission(final int serverID, final long missionCard, final long associatedSectorCard,
                   final Map<Long, MissionCountable> missionCountables)
    {
        this.serverID = serverID;
        this.missionCardGUID = missionCard;
        this.associatedSectorCardGUID = associatedSectorCard;
        this.missionCountables = missionCountables;
        this.lock = new ReentrantLock();
    }

    protected boolean incrementCountBy(final long counterGUID, final long sourceSectorGUID, final double incrementByValue)
    {
        lock.lock();
        try
        {
            if (this.missionState == MissionState.Completed)
                return false;

            final MissionCountable countable = this.missionCountables.get(counterGUID);
            if (countable == null)
            {
                return false;
            }

            if (associatedSectorCardGUID == 0 || associatedSectorCardGUID == sourceSectorGUID)
            {
                final long newCount = (long) (countable.getCurrentCount() + incrementByValue);
                countable.setCount(newCount);
                return true;
            }

            return false;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        lock.lock();
        try
        {
            bw
                    .writeUInt16(this.serverID)
                    .writeGUID(this.missionCardGUID)
                    .writeGUID(this.associatedSectorCardGUID)
                    .writeDescCollection(this.missionCountables.values())
                    .writeDesc(getAndSetMissionState());
        }
        finally
        {
            lock.unlock();
        }
    }

    private MissionState getAndSetMissionState()
    {
        if (missionState != null && missionState == MissionState.Completed)
            return missionState;

        final boolean allMatch = this.missionCountables.values()
                .stream()
                .allMatch(missionCountable -> missionCountable.getCurrentCount() >= missionCountable.getNeedCount());

        if (allMatch)
        {
            setMissionState(MissionState.Completed);
        }
        else
        {
            setMissionState(MissionState.InProgress);
        }

        return missionState;
    }

    @Override
    public int getServerID()
    {
        return this.serverID;
    }

    @Override
    public void setServerID(int serverID)
    {
        lock.lock();
        try
        {
            this.serverID = serverID;
        }
        finally
        {
            lock.unlock();
        }
    }


    public Map<Long, MissionCountable> getMissionCountables()
    {
        return missionCountables;
    }

    public boolean hasCounterGuid(final long counterCardGuid)
    {
        return this.missionCountables.values()
                .stream()
                .anyMatch(missionCountable -> missionCountable.getCounterCardGuid() == counterCardGuid);
    }

    public long getMissionCardGUID()
    {
        return missionCardGUID;
    }

    public void setMissionState(final MissionState missionState)
    {
        this.missionState = missionState;
    }

    public MissionState getMissionState()
    {
        return missionState;
    }

    public long getAssociatedSectorCardGUID()
    {
        return associatedSectorCardGUID;
    }

    public enum MissionState implements IProtocolWrite
    {
        InProgress,
        Completed,
        Submitting;

        public int getValue()
        {
            return this.ordinal();
        }

        @Override
        public void write(BgoProtocolWriter bw)
        {
            bw.writeInt32(this.getValue());
        }
    }

    @Override
    public String toString()
    {
        return "Mission{" +
                "serverID=" + serverID +
                ", missionCardGUID=" + missionCardGUID +
                ", associatedSectorCardGUID=" + associatedSectorCardGUID +
                ", missionCountables=" + missionCountables +
                ", missionState=" + missionState +
                '}';
    }
}
