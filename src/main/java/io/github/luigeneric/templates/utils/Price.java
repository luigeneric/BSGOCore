package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.ResourceType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Price implements IProtocolWrite
{
    private final Map<Long, Float> items;

    public Price(final Map<Long, Float> items)
    {
        this.items = items;
    }
    public Price(final Price price)
    {
        this(new HashMap<>(price.items));
    }
    public Price(final ResourceType resourceType, final long count)
    {
        this();
        this.addItem(resourceType.guid, count);
    }

    public Price()
    {
        this(new HashMap<>());
    }

    public synchronized void addPrice(final Price price, final long count)
    {
        for (final Map.Entry<Long, Float> item : price.items.entrySet())
        {
            final long itemGUID = item.getKey();
            final float countPerCount = item.getValue() * count;

            if (!this.items.containsKey(itemGUID))
            {
                this.items.put(itemGUID, countPerCount);
            }
            else
            {
                final float presentCount = this.items.get(itemGUID);
                this.items.put(itemGUID, presentCount + countPerCount);
            }
        }
    }
    public synchronized float getFor(final long itemGuid)
    {
        return this.items.getOrDefault(itemGuid, 0f);
    }
    public void addPrice(final Price price)
    {
        this.addPrice(price, 1);
    }
    public synchronized void addItem(final long cardGuid, final long count)
    {
        this.items.put(cardGuid, (float) count);
    }

    public synchronized boolean isEmpty()
    {
        return this.items.isEmpty();
    }


    @Override
    public synchronized void write(final BgoProtocolWriter bw)
    {
        final int sizeToWrite = items.size();

        //length optimization
        bw.ensureDeltaCapacity(2 + sizeToWrite * 8);

        bw.writeLength(sizeToWrite);
        for (final Map.Entry<Long, Float> entry : items.entrySet())
        {
            bw.writeGUID(entry.getKey()); //4
            bw.writeSingle(entry.getValue()); //4
        }
    }

    @Override
    public String toString()
    {
        return "Price{" +
                "items=" + items +
                '}';
    }

    public synchronized Map<Long, Float> getItems()
    {
        return Collections.unmodifiableMap(this.items);
    }
}
