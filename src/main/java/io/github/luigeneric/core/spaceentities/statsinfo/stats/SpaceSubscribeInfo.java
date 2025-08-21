package io.github.luigeneric.core.spaceentities.statsinfo.stats;

import io.github.luigeneric.core.movement.IMovementUpdateSubscriber;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.player.skills.SkillBook;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SpaceSubscribeInfo
{
    protected final Owner owner;
    protected final ObjectStats statsFinal;
    protected final HullPowerPoints hullPowerPoints;
    protected final Map<StatsProtocolSubscriber, BasePropertyBuffer> subscribers;
    protected IMovementUpdateSubscriber movementUpdateSubscriber;
    protected final ObjectStats modifiedForStatsBuff;
    protected final Lock lock;

    public SpaceSubscribeInfo(final Owner owner, final ObjectStats statsFinal, final float hullPoints,
                              final float powerPoints)
    {
        this.owner = owner;
        this.statsFinal = statsFinal;
        this.modifiedForStatsBuff = new ObjectStats();
        this.hullPowerPoints = new HullPowerPoints(hullPoints, powerPoints);
        this.subscribers = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    public Optional<IMovementUpdateSubscriber> getMovementUpdateSubscriber()
    {
        return Optional.ofNullable(movementUpdateSubscriber);
    }

    public void setMovementUpdateSubscriber(IMovementUpdateSubscriber movementUpdateSubscriber)
    {
        this.movementUpdateSubscriber = movementUpdateSubscriber;
    }

    public SpaceSubscribeInfo(final long ownerID, final ObjectStats stats)
    {
        this(new Owner(ownerID, false), stats, 0f, 0f);
    }

    protected void resetStats()
    {
        this.statsFinal.clean();
    }

    public void injectOldSubscriber(final BasePropertyBuffer basePropertyBuffer)
    {
        this.subscribers.put(basePropertyBuffer.getStatsProtocolSubscriber(), basePropertyBuffer);
        basePropertyBuffer.onStatInfoChanged(this, StatInfo.Hp);
        basePropertyBuffer.onStatInfoChanged(this, StatInfo.Pp);
        basePropertyBuffer.onStatInfoChanged(this, StatInfo.Stats);
    }
    public void addSubscriber(final StatsProtocolSubscriber statsProtocolSubscriber)
    {
        lock.lock();
        try
        {
            final BasePropertyBuffer tmp = BasePropertyBuffer.create(owner, statsProtocolSubscriber);
            if (tmp == null)
                return;
            this.subscribers.put(statsProtocolSubscriber, tmp);
            tmp.onStatInfoChanged(this, StatInfo.Combat);
            tmp.onStatInfoChanged(this, StatInfo.Stats);
            tmp.onStatInfoChanged(this, StatInfo.Hp);
            tmp.onStatInfoChanged(this, StatInfo.Pp);

            if (this.getShipSlots().isPresent())
            {
                final ShipSlots slots = this.getShipSlots().get();
                for (Map.Entry<Integer, ShipSlot> slot : slots.entrySet())
                {
                    tmp.onSlotUpdate(this, slot.getKey());
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public ObjectStats getModifiedForStatsBuff()
    {
        return this.modifiedForStatsBuff;
    }

    public void removeSubscriber(final StatsProtocolSubscriber statsProtocolSubscriber)
    {
        lock.lock();
        try
        {
            this.subscribers.remove(statsProtocolSubscriber);
        }
        finally
        {
            lock.unlock();
        }
    }
    public void removeAllSubscriber()
    {
        lock.lock();
        try
        {
            this.subscribers.clear();
        }
        finally
        {
            lock.unlock();
        }
    }


    public void setHp(final float newHP)
    {
        lock.lock();
        try
        {
            this.hullPowerPoints.setHullPoints(newHP);
            capMaxHp();
            for (final BasePropertyBuffer value : this.subscribers.values())
            {
                value.onStatInfoChanged(this, StatInfo.Hp);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setPp(final float newPp)
    {
        this.hullPowerPoints.setPowerPoints(newPp);
        ppUpdated();
    }
    private void ppUpdated()
    {
        genericUpdate(StatInfo.Pp);
    }

    protected void genericUpdate(final StatInfo statInfo)
    {
        lock.lock();
        try
        {
            for (final BasePropertyBuffer value : this.subscribers.values())
            {
                value.onStatInfoChanged(this, statInfo);
            }
        }
        finally
        {
            lock.unlock();
        }
    }


    public float getHp()
    {
        return this.hullPowerPoints.getHullPoints();
    }


    public float getPp()
    {
        return this.hullPowerPoints.getPowerPoints();
    }

    public void capMaxHp()
    {
        final float currentHP = this.getHp();
        if (currentHP > this.getStat(ObjectStat.MaxHullPoints))
            setMaxHp();
    }
    public void setMaxHpPp()
    {
        setMaxHp();
        setMaxPp();
    }
    public void setMaxPp()
    {
        this.setPp(statsFinal.getStatOrDefault(ObjectStat.MaxPowerPoints));
    }
    public void setMaxHp()
    {
        this.setHp(statsFinal.getStatOrDefault(ObjectStat.MaxHullPoints));
    }

    protected Optional<CombatInfo> getCombatInfo()
    {
        return Optional.empty();
    }
    public boolean isInCombat()
    {
        final Optional<CombatInfo> optCombatInfo = this.getCombatInfo();
        if (optCombatInfo.isEmpty())
        {
            return false;
        }
        final CombatInfo cInfo = optCombatInfo.get();
        return cInfo.isInCombat();
    }


    public boolean containsStat(ObjectStat stat)
    {
        return this.statsFinal.containsStat(stat);
    }


    public void setSkillBook(final SkillBook skillBook)
    {
    }

    public Map<StatsProtocolSubscriber, BasePropertyBuffer> getSubscribers()
    {
        lock.lock();
        try
        {
            return this.subscribers;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setHpPp(final float newHp, final float newPp)
    {
        this.setHp(newHp);
        this.setPp(newPp);
    }

    public void setLastCombatTime(final long timeStamp, final TimeUnit timeUnit)
    {
        setLastCombatTime(timeUnit.toMillis(timeStamp));
    }
    public void setLastCombatTime(final long timeStampMs)
    {
        this.getCombatInfo().ifPresent(combatInfo ->
        {
            combatInfo.setLastCombatTime(timeStampMs);
        });
    }

    public void updateCombatStatus(final long combatTimer, final long currentTimeStamp, final boolean hasMissile)
    {
        final Optional<CombatInfo> optCombatInfo = this.getCombatInfo();
        if (optCombatInfo.isEmpty()) return;
        final CombatInfo combatInfo = optCombatInfo.get();
        final boolean changed = combatInfo.isCombatStatusChanged(combatTimer, currentTimeStamp, hasMissile);
        if (changed)
            this.subscribers.values().forEach(subscriber -> subscriber.onStatInfoChanged(this, StatInfo.Combat));
    }

    public Float getStat(final ObjectStat stat)
    {
        return this.statsFinal.getStat(stat);
    }

    public float getStatOrDefault(final ObjectStat stat, final float orValue)
    {
        return this.statsFinal.getStatOrDefault(stat, orValue);
    }
    public float getStatOrDefault(final ObjectStat stat)
    {
        return this.statsFinal.getStatOrDefault(stat);
    }

    public ObjectStats getStats()
    {
        return this.statsFinal;
    }


    public void setTargetObjectID(final long newTargetObjectID)
    {
        lock.lock();
        try
        {
            this.getTargetObjectID().ifPresent(target ->
            {
                target.set(newTargetObjectID);
                for (final BasePropertyBuffer sub : this.subscribers.values())
                {
                    sub.onStatInfoChanged(this, StatInfo.Target);
                }
            });
        }
        finally
        {
            lock.unlock();
        }
    }


    public void applyStats()
    {
        this.resetStats();
        //applyFromModBonus();
    }

    protected void applyFromModBonus()
    {
        for (final Map.Entry<ObjectStat, Float> objectStatFloatEntry : this.modifiedForStatsBuff.getAllStats().entrySet())
        {
            //log.info("obj stat " + objectStatFloatEntry);
            final boolean contains = statsFinal.containsStat(objectStatFloatEntry.getKey());
            if (!contains)
                continue;

            final float current = statsFinal.getStat(objectStatFloatEntry.getKey());
            final float newStat = current * (1f + objectStatFloatEntry.getValue());
            statsFinal.setStat(objectStatFloatEntry.getKey(), newStat);
        }
    }


    protected Optional<ShipModifiers> getShipModifiers()
    {
        return Optional.empty();
    }
    public void addModifier(final ShipModifier shipModifier)
    {
        final Optional<ShipModifiers> optModifiers = this.getShipModifiers();
        if (optModifiers.isEmpty()) return;
        final ShipModifiers shipModifiers = optModifiers.get();
        final List<ShipModifier> newModifiers = shipModifiers.addShipModifier(shipModifier);

        for (final BasePropertyBuffer sub : this.subscribers.values())
        {
            sub.onModifierAdd(this, newModifiers);
        }
        this.applyStats();
    }

    public void removeModifiers(final Set<Long> toRemoveModifiers)
    {
        final Optional<ShipModifiers> optModifiers = this.getShipModifiers();
        if (optModifiers.isEmpty()) return;
        final ShipModifiers shipModifiers = optModifiers.get();
        shipModifiers.removeModifier(toRemoveModifiers);

        this.subscribers.values().forEach(sub -> sub.onModifierRemove(this, toRemoveModifiers));
        this.applyStats();
        /* final float boostSpeed = this.statsFinal.getStat(ObjectStat.BoostSpeed); */
    }


    public Optional<AtomicLong> getTargetObjectID()
    {
        return Optional.empty();
    }

    public Optional<ShipSlots> getShipSlots()
    {
        return Optional.empty();
    }


    public Optional<SkillBook> getSkillBook()
    {
        return Optional.empty();
    }


    public Optional<ShipModifiers> getModifiers()
    {
        return Optional.empty();
    }


    public void setShipSlots(final ShipSlots slots) {    }


    public Owner getOwner()
    {
        return this.owner;
    }


    public void sendSpacePropertyBuffer()
    {
        final Set<StatsProtocolSubscriber> toRemove = new HashSet<>();

        lock.lock();
        try
        {
            for (final Map.Entry<StatsProtocolSubscriber, BasePropertyBuffer> entry : this.subscribers.entrySet())
            {
                if (entry.getValue().isUpdated())
                {
                    final boolean sendResult = entry.getKey().sendSpacePropertyBuffer(entry.getValue());
                    if (!sendResult)
                    {
                        toRemove.add(entry.getKey());
                    }
                }
            }

            for (StatsProtocolSubscriber statsProtocolSubscriber : toRemove)
            {
                subscribers.remove(statsProtocolSubscriber);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void removeSubscriberWithId(final long playerId)
    {
        final List<StatsProtocolSubscriber> forRemoval = new ArrayList<>();

        lock.lock();
        try
        {
            for (Map.Entry<StatsProtocolSubscriber, BasePropertyBuffer> subscriberBuffer : this.subscribers.entrySet())
            {
                if (subscriberBuffer.getKey().userId() == playerId)
                {
                    forRemoval.add(subscriberBuffer.getKey());
                }
            }
            forRemoval.forEach(subscribers::remove);
        }
        finally
        {
            lock.unlock();
        }
    }
}
