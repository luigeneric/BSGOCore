package io.github.luigeneric.core.player;


import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Hangar extends InfoPublisher<HangarShipsUpdate>
{
    /**
     * A map of ushort as the key and the HangarShip as the value
     */
    private final Map<Integer, HangarShip> ships;
    private int activeShipIndex;
    private final ReadWriteLock readWriteLock;

    public Hangar(final Map<Integer, HangarShip> ships, final long playerID)
    {
        super(InfoType.Ships, playerID, new HangarShipsUpdate("", List.of()));
        this.ships = ships;
        this.activeShipIndex = -1;
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public Hangar(final long playerID)
    {
        this(new ConcurrentHashMap<>(), playerID);
    }

    private void readLock()
    {
        this.readWriteLock.readLock().lock();
    }
    private void writeLock()
    {
        this.readWriteLock.writeLock().lock();
    }
    private void readUnlock()
    {
        this.readWriteLock.readLock().unlock();;
    }
    private void writeUnlock()
    {
        this.readWriteLock.writeLock().unlock();
    }

    public void addHangarShip(final HangarShip newHangarShip)
    {
        writeLock();
        try
        {
            //empty hangar? -> set new ship to active ship
            if (this.ships.size() == 0)
            {
                this.activeShipIndex = newHangarShip.getServerId();
            }
            this.ships.put(newHangarShip.getServerId(), newHangarShip);

            hangarShipsUpdate();
        }
        finally
        {
            writeUnlock();
        }
    }

    private void hangarShipsUpdate()
    {
        this.set(new HangarShipsUpdate(getActiveShip().getName(), getSortedGUIDs()));
    }

    public boolean hasActiveShip()
    {
        readLock();
        try
        {
            if (this.activeShipIndex == -1)
            {
                if (this.ships.size() == 0)
                {
                    return false;
                }
                return false;
            }
            return true;
        }
        finally
        {
            readUnlock();
        }

    }
    public HangarShip getActiveShip() throws IllegalCallerException, IllegalStateException
    {
        readLock();
        try
        {
            if (this.activeShipIndex == -1)
            {
                if (this.ships.size() == 0)
                {
                    throw new IllegalCallerException("This player has no hangar ships yet");
                }
                throw new IllegalStateException("Active ship is not set");
            }
            return this.ships.get(this.activeShipIndex);
        }
        finally
        {
            readUnlock();
        }
    }

    public List<Long> getSortedGUIDs()
    {
        readLock();
        try
        {
            final List<Long> ids = new ArrayList<>();

            ids.add(this.getActiveShip().getCardGuid());
            for (HangarShip value : this.ships.values())
            {
                if (this.getActiveShip().getCardGuid() == value.getCardGuid())
                    continue;
                ids.add(value.getCardGuid());
            }

            return ids;
        }
        finally
        {
            readUnlock();
        }
    }
    public void setActiveShipIndex(final int index)
    {
        writeLock();
        try
        {
            if (this.ships.containsKey(index))
            {
                this.activeShipIndex = index;
                hangarShipsUpdate();
            }
        }
        finally
        {
            writeUnlock();
        }
    }

    public HangarShip getByServerId(int serverID)
    {
        readLock();
        try
        {
            return this.ships.get(serverID);
        }
        finally
        {
            readUnlock();
        }
    }

    public List<HangarShip> getAllHangarShips()
    {
        readLock();
        try
        {
            return ships.values().stream().toList();
        }
        finally
        {
            readUnlock();
        }
    }

    @Override
    public String toString()
    {
        return "Hangar{" +
                "ships=" + ships +
                ", activeShipIndex=" + activeShipIndex +
                '}';
    }

    public void removeAllHangarShips()
    {
        writeLock();
        try
        {
            this.ships.clear();
        }
        finally
        {
            writeUnlock();
        }
    }
}
