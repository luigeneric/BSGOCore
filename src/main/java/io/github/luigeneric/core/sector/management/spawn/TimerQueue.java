package io.github.luigeneric.core.sector.management.spawn;



import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ScheduleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimerQueue<E>
{
    protected final PriorityQueue<ScheduleItem<E>> priorityQueue;
    protected final Lock lock;

    public TimerQueue(PriorityQueue<ScheduleItem<E>> priorityQueue,
                      final Lock lock)
    {
        this.priorityQueue = priorityQueue;
        this.lock = lock;
    }

    public TimerQueue()
    {
        this(new PriorityQueue<>(), new ReentrantLock());
    }

    private boolean hasItemWithTimeout(final Tick current)
    {
        if (this.priorityQueue.isEmpty()) return false;

        final ScheduleItem<E> jumpScheduleItem = this.priorityQueue.peek();
        final long delta = jumpScheduleItem.getTimeStamp() - current.getTimeStamp();
        return delta <= 0;
    }

    public List<ScheduleItem<E>> getAllTimeoutItems(final Tick current)
    {
        lock.lock();
        try
        {
            if (!this.hasItemWithTimeout(current))
                return List.of();

            final List<ScheduleItem<E>> rLst = new ArrayList<>();
            while (this.hasItemWithTimeout(current))
            {
                ScheduleItem<E> tmp = priorityQueue.poll();
                rLst.add(tmp);
            }
            return rLst;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     *
     * @param currentTick tick of the sector
     * @param e item to be scheduled
     * @param scheduleDelay delay in seconds
     */
    public void enqueue(final Tick currentTick, final E e, final float scheduleDelay)
    {
        Objects.requireNonNull(currentTick);
        Objects.requireNonNull(e);

        lock.lock();
        try
        {
            this.priorityQueue.offer(new ScheduleItem<>(currentTick, scheduleDelay, e));
        }
        finally
        {
            lock.unlock();
        }
    }

    public ScheduleItem<E> getItem()
    {
        lock.lock();
        try
        {
            if (this.priorityQueue.isEmpty()) throw new IllegalStateException("Could not call getItem because queue is empty!");
            return this.priorityQueue.poll();
        }
        finally
        {
            lock.unlock();
        }
    }
}
