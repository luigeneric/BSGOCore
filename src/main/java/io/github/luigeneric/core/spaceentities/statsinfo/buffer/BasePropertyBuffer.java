package io.github.luigeneric.core.spaceentities.statsinfo.buffer;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates.*;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.*;
import io.github.luigeneric.templates.utils.ObjectStat;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BasePropertyBuffer implements IProtocolWrite, ISpaceInfoSubscriber
{
    protected final Owner owner;
    protected final Set<PropertyUpdate> propertyUpdates;
    protected final Map<SpaceUpdateType, PropertyUpdate> uniquePropertyUpdates;
    protected final StatsProtocolSubscriber statsProtocolSubscriber;
    private final Lock lock;


    public BasePropertyBuffer(Owner owner, StatsProtocolSubscriber statsProtocolSubscriber)
    {
        this.owner = owner;
        this.statsProtocolSubscriber = statsProtocolSubscriber;
        this.propertyUpdates = new HashSet<>();
        this.uniquePropertyUpdates = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public static BasePropertyBuffer create(final Owner owner, final StatsProtocolSubscriber statsProtocolSubscriber)
    {
        switch (statsProtocolSubscriber.getProtocolID())
        {
            case Player ->
            {
                return new PlayerPropertyBuffer(owner, statsProtocolSubscriber);
            }
            case Subscribe ->
            {
                return new SubscribeProtocolBuffer(owner, statsProtocolSubscriber);
            }
            case Game ->
            {
                return new SpacePropertyBuffer(owner, statsProtocolSubscriber);
            }
            default ->
            {
                return null;
            }
        }
    }
    protected abstract boolean isPropertyUpdateAllowed(final PropertyUpdate propertyUpdate);

    private void addUpdate(final PropertyUpdate propertyUpdate)
    {
        if (!isPropertyUpdateAllowed(propertyUpdate))
            return;


        switch (propertyUpdate.getSpaceUpdateType())
        {
            case HullPoints, PowerPoints, TargetID, CombatStatus ->
            {
                this.uniquePropertyUpdates.put(propertyUpdate.getSpaceUpdateType(), propertyUpdate);
            }
            default ->
            {
                if (propertyUpdates.contains(propertyUpdate))
                {
                    this.propertyUpdates.remove(propertyUpdate);
                }
                this.propertyUpdates.add(propertyUpdate);
            }
        }
    }

    public boolean isUpdated()
    {
        lock.lock();
        try
        {
            return !(this.propertyUpdates.isEmpty() && this.uniquePropertyUpdates.isEmpty());
        }
        finally
        {
            lock.unlock();
        }
    }

    private List<PropertyUpdate> getAllUpdates()
    {
        final List<PropertyUpdate> updates = new ArrayList<>(propertyUpdates);
        this.propertyUpdates.clear();
        updates.addAll(this.uniquePropertyUpdates.values());
        this.uniquePropertyUpdates.clear();
        return updates;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        lock.lock();
        try
        {
            final List<PropertyUpdate> allUpdates = this.getAllUpdates();
            bw.writeDescCollection(allUpdates);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void onStatInfoChanged(final SpaceSubscribeInfo spaceSubscribeInfo, final StatInfo statInfo)
    {
        lock.lock();
        try
        {
            switch (statInfo)
            {
                case Hp ->
                {
                    this.addUpdate(new HpUpdate(spaceSubscribeInfo.getHp()));
                }
                case Pp ->
                {
                    this.addUpdate(new PpUpdate(spaceSubscribeInfo.getPp()));
                }
                case Combat ->
                {
                    this.addUpdate(new CombatUpdate(spaceSubscribeInfo.isInCombat()));
                }
                case Target ->
                {
                    spaceSubscribeInfo.getTargetObjectID().ifPresent(target ->
                    {
                        this.addUpdate(new TargetIDUpdate(target.get()));
                    });
                }
                case Stats ->
                {
                    for (Map.Entry<ObjectStat, Float> objectStatFloatEntry :
                            spaceSubscribeInfo.getStats().getAllStats().entrySet())
                    {
                        this.addUpdate(new ObjectStatUpdate(objectStatFloatEntry.getKey(), objectStatFloatEntry.getValue()));
                    }

                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void onSlotUpdate(final SpaceSubscribeInfo spaceSubscribeInfo, final int slotID)
    {
        final Optional<ShipSlots> optSlots = spaceSubscribeInfo.getShipSlots();
        if (optSlots.isEmpty()) return;
        final ShipSlots slots = optSlots.get();
        final ShipSlot slot = slots.getSlot(slotID);

        lock.lock();
        try
        {
            if (slot.getShipSystem() == null)
                return;

            if (slot.getShipAbility() == null)
                return;

            for (Map.Entry<ObjectStat, Float> stats : slot.getShipAbility().getItemBuffAdd().getAllStats().entrySet())
            {
                this.addUpdate(new SlotUpdate(slotID, stats.getKey(), stats.getValue()));
            }
            //workaround for mining cannons min range
            this.addUpdate(
                    new SlotUpdate(
                            slotID,
                            ObjectStat.MinRange,
                            slot.getShipAbility().getItemBuffAdd().getStatOrDefault(ObjectStat.MinRange)
                    )
            );
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void onModifierAdd(final SpaceSubscribeInfo spaceSubscribeInfo, final List<ShipModifier> newBuffs)
    {
        lock.lock();
        try
        {
            for (final ShipModifier newBuff : newBuffs)
            {
                this.propertyUpdates.add(new AddModifierUpdate(newBuff));
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void onModifierRemove(final SpaceSubscribeInfo spaceSubscribeInfo, final Set<Long> removedBuffIDs)
    {
        lock.lock();
        try
        {
            for (final long removedBuffID : removedBuffIDs)
            {
                this.propertyUpdates.add(new RemoveModifierUpdate(removedBuffID));
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public Owner getOwner()
    {
        return owner;
    }

    @Override
    public String toString()
    {
        return "BasePropertyBuffer{" +
                "owner=" + owner +
                ", propertyUpdates=" + propertyUpdates +
                ", uniquePropertyUpdates=" + uniquePropertyUpdates +
                ", statsProtocolSubscriber=" + statsProtocolSubscriber +
                '}';
    }

    public StatsProtocolSubscriber getStatsProtocolSubscriber()
    {
        return statsProtocolSubscriber;
    }
}
