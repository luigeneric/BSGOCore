package io.github.luigeneric.core.player.factors;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.FactorSource;
import io.github.luigeneric.enums.FactorType;
import io.github.luigeneric.utils.FreeUint16Counter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Factors implements IProtocolWrite
{
    private final long playerID;
    private final Map<Integer, Factor> factorMap;
    private final FreeUint16Counter freeUint16Counter;
    private final ReadWriteLock readWriteLock;
    private final float boostLimiter;
    private FactorSubscriber factorSubscriber;

    /*
    public Factors(final Map<Integer, Factor> factorMap, final float boostLimiter)
    {
        this.factorMap = factorMap;
        this.boostLimiter = boostLimiter;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.freeUint16Counter = new FreeUint16Counter();
    }
     */
    public Factors(final long playerID)
    {
        this(playerID, new HashMap<>(), new FreeUint16Counter(), new ReentrantReadWriteLock(), 2);
    }


    private int getNextFreeId()
    {
        int freeId = freeUint16Counter.getFreeId();
        while (this.factorMap.containsKey(freeId))
        {
            freeId = freeUint16Counter.getFreeId();
        }
        return freeId;
    }

    /**
     *
     * @param source FactorSource
     * @param factorType FactorType
     * @return how much left, if
     */
    public float getFactorLimitLeft(final FactorSource source, final FactorType factorType)
    {
        rLock();
        try
        {
            final float currentSourceBonus = this.factorMap.values().stream()
                    .filter(f -> f.getFactorSource() == source)
                    .filter(f -> f.getFactorType() == factorType)
                    .map(Factor::getValue)
                    .reduce((float) 1, Float::sum);
            return boostLimiter - currentSourceBonus;
        }
        finally
        {
            rUnlock();
        }
    }
    private void rLock()
    {
        readWriteLock.readLock().lock();
    }
    private void rUnlock()
    {
        readWriteLock.readLock().unlock();;
    }
    private void wLock()
    {
        readWriteLock.writeLock().lock();
    }
    private void wUnlock()
    {
        readWriteLock.writeLock().unlock();;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        rLock();
        try
        {
            bw.writeDescCollection(this.factorMap.values());
        }
        finally
        {
            rUnlock();
        }

    }

    public float getMultiplierFor(final FactorType factorType)
    {
        rLock();
        try
        {
            final var now = LocalDateTime.now(Clock.systemUTC());
            return this.factorMap.values()
                    .stream()
                    .filter(factor -> factor.getFactorType() == factorType)
                    .filter(factor -> factor.getEndTime().isAfter(now))
                    .map(Factor::getValue)
                    .reduce((float) 1, Float::sum);
        }
        finally
        {
            rUnlock();
        }
    }

    public Map<FactorType, Float> getMultiplicatorsForLimitLeft(final FactorSource factorSource)
    {
        rLock();
        try
        {
            final Map<FactorType, Float> typeMap = new HashMap<>();
            List<Factor> tmp = this.factorMap.values()
                    .stream()
                    .filter(factor -> factor.getFactorSource() == factorSource)
                    .toList();
            for (Factor factor : tmp)
            {
                if (typeMap.containsKey(factor.getFactorType()))
                {
                    float previous = typeMap.get(factor.getFactorType());
                    final float current = factor.getValue() + previous;
                    typeMap.put(factor.getFactorType(), current);
                }
                //empty
                else
                {
                    typeMap.put(factor.getFactorType(), factor.getValue());
                }
            }
            final Map<FactorType, Float> limitLeftMap = new HashMap<>();
            for (Map.Entry<FactorType, Float> factorTypeFloatEntry : typeMap.entrySet())
            {
                limitLeftMap.put(factorTypeFloatEntry.getKey(), boostLimiter - factorTypeFloatEntry.getValue());
            }

            return limitLeftMap;
        }
        finally
        {
            rUnlock();
        }
    }

    public Collection<Factor> values()
    {
        rLock();
        try
        {
            return this.factorMap.values();
        }
        finally
        {
            rUnlock();
        }
    }

    public boolean hasExperienceFor(final LootType lootType)
    {
        rLock();
        try
        {
            for (final Factor value : this.factorMap.values())
            {
                final boolean overallXpFlag = value.getFactorType() == FactorType.Experience;
                if (overallXpFlag)
                    return true;

                boolean otherFlag;
                switch (lootType)
                {
                    case PVE ->
                    {
                        otherFlag = value.getFactorType() == FactorType.PVE_XP;
                    }
                    case PVP ->
                    {
                        otherFlag = value.getFactorType() == FactorType.PVP_XP;
                    }
                    case Reward_ASSIGNMENT ->
                    {
                        otherFlag = value.getFactorType() == FactorType.Reward_ASSIGNMENT_XP;
                    }
                    case DutyXP ->
                    {
                        otherFlag = value.getFactorType() == FactorType.DutyXP;
                    }
                    default ->
                    {
                        otherFlag = false;
                    }
                }
                return otherFlag;
            }

            return false;
        }
        finally
        {
            rUnlock();
        }
    }

    public void addFactor(final Factor factor)
    {
        wLock();
        try
        {
            int nextId = getNextFreeId();
            while (factorMap.containsKey(nextId))
            {
                nextId = getNextFreeId();
            }
            factor.setServerID(nextId);
            this.factorMap.put(nextId, factor);
            if (factorSubscriber != null)
                factorSubscriber.notifyFactorStarted(this.playerID, factor);
        }
        finally
        {
            wUnlock();
        }
    }

    public List<Factor> getItemsOfSource(final FactorSource factorSource)
    {
        wLock();
        try
        {
            return factorMap.values()
                    .stream()
                    .filter(factor -> factor.getFactorSource() == factorSource)
                    .toList();
        }
        finally
        {
            wUnlock();
        }
    }
    public void removeItem(final int id)
    {
        wLock();
        try
        {
            this.factorMap.remove(id);
        }
        finally
        {
            wUnlock();
        }
    }
    public Set<Integer> removeExpiredItems()
    {
        wLock();
        try
        {
            final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
            final Set<Integer> expiredIds = this.factorMap.values().stream()
                    .filter(factor -> factor.getEndTime().isBefore(now))
                    .map(Factor::getServerID)
                    .collect(Collectors.toSet());

            if (expiredIds.isEmpty())
                return expiredIds;

            for (final Integer expiredId : expiredIds)
            {
                this.factorMap.remove(expiredId);
            }

            return expiredIds;
        }
        finally
        {
            wUnlock();
        }
    }

    public float getBoostLimiter()
    {
        return boostLimiter;
    }

    public boolean containsId(int removedId)
    {
        rLock();
        try
        {
            return this.factorMap.containsKey(removedId);
        }
        finally
        {
            rUnlock();
        }
    }

    public FactorSubscriber getFactorSubscriber()
    {
        return factorSubscriber;
    }

    public void setFactorSubscriber(FactorSubscriber factorSubscriber)
    {
        this.factorSubscriber = factorSubscriber;
    }

    public boolean hasFactorSource(FactorSource factorSource)
    {
        rLock();
        try
        {
            return this.factorMap.values().stream().anyMatch(factor -> factor.getFactorSource() == factorSource);
        }
        finally
        {
            rUnlock();
        }
    }

    public void removeAll()
    {
        wLock();
        try
        {
            this.factorMap.clear();
        }
        finally
        {
            wUnlock();
        }
    }

    public enum LootType
    {
        PVE,
        PVP,
        Reward_ASSIGNMENT,
        DutyXP;
    }
}
