package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Planetoid extends Asteroid
{
    private MiningShip miningShip;
    private long lastTimeMiningShipAdded;
    private final Lock lock;

    public Planetoid(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, SpaceEntityType spaceEntityType,
                     Faction faction, FactionGroup factionGroup, final SpaceSubscribeInfo spaceSubscribeInfo, float radius)
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, spaceSubscribeInfo, radius, 0f);
        this.lastTimeMiningShipAdded = 0;
        this.lock = new ReentrantLock();
    }
    public Planetoid(long objectID, final OwnerCard ownerCard, final WorldCard worldCard,
                     SpaceSubscribeInfo spaceSubscribeInfo, final float radius)
    {
        this(objectID, ownerCard, worldCard, SpaceEntityType.Planetoid, Faction.Neutral,
                FactionGroup.Group0, spaceSubscribeInfo, radius);
    }

    public long getLastTimeMiningShipAdded()
    {
        return lastTimeMiningShipAdded;
    }

    public boolean hasMiningShip()
    {
        lock.lock();
        try
        {
            if (this.miningShip != null)
            {
                return !this.miningShip.isRemoved();
            }
            return false;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setMiningShip(final MiningShip miningShip, final Tick tick)
    {
        lock.lock();
        try
        {
            this.miningShip = miningShip;
            this.lastTimeMiningShipAdded = tick.getTimeStamp();
        }
        finally
        {
            lock.unlock();
        }
    }
}
