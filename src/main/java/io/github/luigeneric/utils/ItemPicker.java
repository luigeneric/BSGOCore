package io.github.luigeneric.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ItemPicker<Item>
{
    private final Map<Item, Integer> probabilityMap;
    private int maxProbability;
    @Getter
    private final BgoRandom rnd;
    private final Lock lock;

    public ItemPicker(final BgoRandom random)
    {
        this.rnd = random;
        this.probabilityMap = new HashMap<>();
        this.maxProbability = 0;
        this.lock = new ReentrantLock();
    }
    public ItemPicker()
    {
        this(new BgoRandom());
    }


    public void add(final Item item, final int probability)
    {
        Objects.requireNonNull(item, "Item cannot be null");

        lock.lock();
        try
        {
            if (probability < 1)
            {
                return;
            }

            this.probabilityMap.put(item, probability + maxProbability);
            this.maxProbability += probability;
        }
        finally
        {
            lock.unlock();
        }
    }
    public void add(final Item item, final float probability)
    {
        this.add(item, (int)probability * 100);
    }


    /**
     * Adds all items with the same probability to get chosen
     * @param items
     * @param probability
     */
    public void addAll(final List<? extends Item> items, int probability)
    {
        for (final Item item : items)
        {
            add(item, probability);
        }
    }

    public Item getRandomItem() throws IllegalArgumentException, IllegalStateException
    {
        lock.lock();
        try
        {
            if (this.probabilityMap.isEmpty())
            {
                throw new IllegalArgumentException("The pool of objects to choose is empty!");
            }
            int rndResult = rnd.getRndBetweenInt(0, maxProbability);

            final int res = this.probabilityMap.values().stream()
                    .filter(i -> i >= rndResult)
                    .sorted()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("This should never happen"));

            for (var entry : this.probabilityMap.entrySet())
            {
                if (entry.getValue() == res)
                {
                    return entry.getKey();
                }
            }

            throw new IllegalStateException("This should never happen");
        }
        finally
        {
            lock.unlock();
        }
    }
}
