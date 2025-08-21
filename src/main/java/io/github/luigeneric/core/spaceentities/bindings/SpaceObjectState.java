package io.github.luigeneric.core.spaceentities.bindings;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SpaceObjectState implements IProtocolWrite
{
    @Getter
    private long objectID;
    private long revision;
    private boolean changed;
    private boolean isMarkedByCarrier;
    /**
     * Op-Mode
     */
    private boolean isFortified;
    /**
     * Probably Stealth-Mode
     */
    private boolean isCloaked;
    /**
     * Thunder FX
     */
    private boolean isEmpOn;
    /**
     * Loot-container symbol
     */
    private int cargoVolume;
    private boolean isDocking;
    private final Lock lock;

    private final Set<Long> anchoredUserIds;
    public SpaceObjectState(long objectID, long revision, boolean isMarkedByCarrier, boolean isFortified,
                            boolean isCloaked, boolean isEmpOn, int cargoVolume, boolean isDocking
    )
    {
        this.changed = false;
        this.objectID = objectID;
        this.revision = revision;
        this.isMarkedByCarrier = isMarkedByCarrier;
        this.isFortified = isFortified;
        this.isCloaked = isCloaked;
        this.isEmpOn = isEmpOn;
        this.cargoVolume = cargoVolume;
        this.isDocking = isDocking;
        this.anchoredUserIds = new CopyOnWriteArraySet<>();
        this.lock = new ReentrantLock();
    }

    public static SpaceObjectState createForObjectID(final long objectID)
    {
        return new SpaceObjectState(objectID,
                0,
                false,
                false,
                false,
                false,
                0,
                false
        );
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        this.lock.lock();

        try
        {
            this.revision++;
            bw.writeUInt32(objectID);
            bw.writeUInt32(this.revision);
            bw.writeBoolean(isMarkedByCarrier);
            bw.writeBoolean(isFortified);
            bw.writeSingle(0f); //BaseSignature, not used anymore
            bw.writeBoolean(isCloaked);
            bw.writeBoolean(isEmpOn); //short circuited
            bw.writeByte((byte) cargoVolume);

        }finally
        {
            this.lock.unlock();
        }
    }

    public boolean getIsMarkedByCarrier()
    {
        return isMarkedByCarrier;
    }


    public void addAnchoredId(final long playerId)
    {
        this.anchoredUserIds.add(playerId);
    }

    /**
     * Removes the playerid from the anchored set
     * @param playerId
     * @return true if it contained the playerId
     */
    public boolean removeAnchoredId(final long playerId)
    {
        return this.anchoredUserIds.remove(playerId);
    }
    public boolean getIsEmpOn()
    {
        return isEmpOn;
    }

    public void setEmpOn(final boolean empOn)
    {
        lock.lock();
        try
        {
            changed = true;
            isEmpOn = empOn;
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean getIsDocking()
    {
        return isDocking;
    }

    public void setDocking(final boolean docking)
    {
        isDocking = docking;
    }

    public void setMarkedByCarrier(boolean markedByCarrier)
    {
        lock.lock();
        try
        {
            changed = true;
            isMarkedByCarrier = markedByCarrier;
        }
        finally
        {
            lock.unlock();
        }

    }

    public boolean isChanged()
    {
        lock.lock();
        try
        {
            return changed;
        }
        finally
        {
            lock.unlock();
        }
    }
}
