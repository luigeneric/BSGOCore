package io.github.luigeneric.core.player.counters;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.CounterCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.utils.publishersubscriber.Subscriber;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class Counters implements IProtocolWrite, Subscriber<CounterDesc>
{
    private final Map<Long, CounterDesc> counterMap;
    private final ReadWriteLock readWriteLock;
    private final Set<CounterDesc> countersRequiredToUpdate;
    private final Catalogue catalogue;

    private Counters(final Map<Long, CounterDesc> counterMap,
                     final Set<CounterDesc> countersRequiredToUpdate,
                     final long userID
    )
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.counterMap = counterMap;
        this.countersRequiredToUpdate = countersRequiredToUpdate;
        this.readWriteLock = new ReentrantReadWriteLock();
        MDC.put("userID", String.valueOf(userID));
    }
    public static Counters create(final long userID)
    {
        final Counters tmpCounters = new Counters(new HashMap<>(), new CopyOnWriteArraySet<>(), userID);
        tmpCounters.setupZeroCountersFromCards();

        return tmpCounters;
    }

    private void readLock() {
        readWriteLock.readLock().lock();
    }
    private void readUnlock() {
        readWriteLock.readLock().unlock();
    }
    private void writeLock() {
        readWriteLock.writeLock().lock();
    }
    private void writeUnlock() {
        readWriteLock.writeLock().unlock();
    }


    public Map<Long, CounterDesc> getInternalReadOnly()
    {
        return Collections.unmodifiableMap(this.counterMap);
    }
    public void setupZeroCountersFromCards()
    {
        writeLock();
        try
        {
            final List<CounterCard> counterCards = catalogue.getAllCardsOfView(CardView.Counter);
            for (final CounterCard card : counterCards)
            {
                if (card.getCardGuid() == 130920111)
                {
                    log.error("Critical: counter init is inside setupZeroCountersFromCards");
                }
                final CounterDesc desc = new CounterDesc(card.getCardGuid(), 0);
                desc.addSubscriber(this);
                this.counterMap.put(card.getCardGuid(), desc);
            }
        }
        finally
        {
            writeUnlock();
        }
    }

    public void injectOldCounters(final long guid, final double newValue)
    {
        final CounterDesc existing = this.counterMap.get(guid);
        if (existing != null)
        {
            existing.setValue(newValue);
        }
        else
        {
            final Optional<CounterCard> optCounter = catalogue.fetchCard(guid, CardView.Counter);
            if (optCounter.isEmpty())
            {
                log.info("Counter card is zero, abort injecting! guid: " + guid + " newVal: " + newValue);
                return;
            }
            final CounterDesc tmp = new CounterDesc(guid, newValue);
            tmp.addSubscriber(this);
            this.counterMap.put(tmp.getGuid(), tmp);
        }
    }

    public void addCounterOf(final long cardGuid, final double deltaAdd)
    {
        writeLock();
        try
        {
            final CounterDesc existing = this.counterMap.get(cardGuid);
            if (existing != null)
            {
                existing.addValue(deltaAdd);
            }
        }
        finally
        {
            writeUnlock();
        }
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        readLock();
        try
        {
            bw.writeDescCollection(this.countersRequiredToUpdate);
            this.countersRequiredToUpdate.clear();
        }
        finally
        {
            readUnlock();
        }
    }

    @Override
    public void onUpdate(final CounterDesc arg)
    {
        this.countersRequiredToUpdate.add(arg);
    }

    @Override
    public String toString()
    {
        readLock();
        try
        {
            return "Counters{" +
                    "counterMap=" + counterMap +
                    '}';
        }
        finally
        {
            readUnlock();
        }
    }

    public boolean requireUpdate()
    {
        return !this.countersRequiredToUpdate.isEmpty();
    }
    public void initAllUpdate()
    {
        writeLock();
        try
        {
            this.countersRequiredToUpdate.addAll(this.counterMap.values());
        }
        finally
        {
            writeUnlock();
        }
    }
}
