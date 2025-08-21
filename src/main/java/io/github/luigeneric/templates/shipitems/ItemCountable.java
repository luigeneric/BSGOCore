package io.github.luigeneric.templates.shipitems;

import com.google.gson.annotations.Expose;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.linearalgebra.utility.Mathf;

import java.util.concurrent.atomic.AtomicLong;

public class ItemCountable extends ShipItem
{
    @Expose(serialize = false)
    public static final long MAX_VALUE = 4294967295L;
    @Expose(serialize = false)
    public static final long MIN_VALUE = 0L;
    private final AtomicLong count;

    private ItemCountable(final long cardGuid, final long count, final int serverId) throws IllegalArgumentException
    {
        super(cardGuid, ItemType.Countable, serverId);
        if (count < 0) throw new IllegalArgumentException("count cannot be less than 0!");
        this.count = new AtomicLong();
        this.updateCount(count);
    }

    public static ItemCountable fromGUID(final long guid, final float number)
    {
        return fromGUID(guid, (long) number);
    }

    public static ItemCountable fromGUID(final ResourceType resourceType, final long count)
    {
        return fromGUID(resourceType.guid, count);
    }
    public static ItemCountable fromGUID(final long guid, final long count)
    {
        return new ItemCountable(guid, count, 0);
    }

    public ItemCountable(final ItemCountable toCopy)
    {
        this(toCopy.cardGuid, toCopy.getCount(), toCopy.getServerID());
    }

    public static ItemCountable placeHolder()
    {
        return ItemCountable.fromGUID(0, 0);
    }

    @Override
    public ItemCountable copy()
    {
        return new ItemCountable(this);
    }

    /**
     * Updates the current count by setting count to newCount
     * @param newCount the new value
     */
    public void updateCount(final long newCount)
    {
        this.count.set(Mathf.clampSafe(newCount, MIN_VALUE, MAX_VALUE));
    }
    public void incrementCount(final long incrementValue)
    {
        if (incrementValue < 0)
            return;

        final long newCount = this.count.get() + incrementValue;
        updateCount(newCount);
    }
    public void decrementCount(final long decrementValue)
    {
        final long newCount = this.count.get() - decrementValue;
        this.updateCount(newCount);
    }

    public long getCount()
    {
        return this.count.get();
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(count.get());
    }

    @Override
    public String toString()
    {
        return "ItemCountable{" +
                "count=" + count +
                ", cardGuid=" + cardGuid +
                ", itemType=" + itemType +
                ", serverID=" + this.getServerID() +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ItemCountable that = (ItemCountable) o;

        return count.get() == that.count.get();
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + Long.hashCode(count.get());
        return result;
    }
}
