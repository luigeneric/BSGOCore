package io.github.luigeneric.core.player.container;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ItemType;
import io.github.luigeneric.templates.shipitems.ShipItem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public abstract class ItemList extends IContainer
{
    protected final Map<Integer, ShipItem> items;
    protected final ReadWriteLock readWriteLock;

    public ItemList(final IContainerID containerID, final List<ShipItem> items, final long userID)
    {
        super(containerID);
        this.items = new HashMap<>();
        MDC.put("userID", String.valueOf(userID));
        this.readWriteLock = new ReentrantReadWriteLock();

        Objects.requireNonNull(items, "items cannot be null");
        addShipItems(items);
    }
    public ItemList(final IContainerID containerID, final long userID)
    {
        this(containerID, List.of(), userID);
    }

    protected void readLock()
    {
        this.readWriteLock.readLock().lock();
    }
    protected void writeLock()
    {
        this.readWriteLock.writeLock().lock();
    }
    protected void readUnlock()
    {
        this.readWriteLock.readLock().unlock();;
    }
    protected void writeUnlock()
    {
        this.readWriteLock.writeLock().unlock();
    }


    @Override
    public ShipItem addShipItem(final ShipItem shipItem)
    {
        writeLock();
        try
        {
            if (shipItem instanceof ItemCountable itemCountable)
            {
                if (itemCountable.getCount() == 0)
                {
                    return shipItem;
                }
            }

            final Optional<ItemCountable> existingCountable;
            if (shipItem.getItemType() == ItemType.Countable && (shipItem instanceof ItemCountable newCountable)
                    && (existingCountable = hasItemCountable(shipItem.getCardGuid())).isPresent())
            {
                if (containerID.getContainerType() != ContainerType.Shop)
                {
                    log.info("add item {} to {}, increment count by {}",
                            newCountable.getCardGuid(),
                            this.containerID.getContainerType(),
                            newCountable.getCount()
                    );
                }
                log.info("existing countable {} old value {}", existingCountable.get().getCardGuid(), existingCountable.get().getCount());
                existingCountable.get().incrementCount(newCountable.getCount());
                log.info("new count of {} is {}", existingCountable.get().getCardGuid(), existingCountable.get().getCount());
                return existingCountable.get();
            }
            else
            {
                log.info("add item {} to {}",
                        shipItem,
                        this.containerID.getContainerType()
                );
                final int serverID = this.getFreeServerID();
                shipItem.setServerID(serverID);
                this.items.put(serverID, shipItem);
                return shipItem;
            }
        }
        catch (Exception ex)
        {
            log.error("In addShipItem {}", shipItem.getCardGuid());
            return null;
        }
        finally
        {
            writeUnlock();
        }
    }
    public void addShipItems(final Collection<? extends ShipItem> items)
    {
        for (final ShipItem item : items)
        {
            addShipItem(item);
        }
    }


    @Override
    public ShipItem removeShipItem(int itemID)
    {
        writeLock();
        try
        {
            return this.items.remove(itemID);
        }
        finally
        {
            writeUnlock();
        }

    }
    public void removeAllShipItems()
    {
        writeLock();
        try
        {
            this.items.clear();
        }
        finally
        {
            writeUnlock();
        }
    }

    private int getFreeServerID()
    {
        for (int i = 0; i < (Short.MAX_VALUE * 2); i++)
        {
            if (!this.items.containsKey(i))
            {
                return i;
            }
        }
        throw new IllegalStateException("Can not add, no free ID found");
    }

    public boolean hasItem(final ShipItem shipItem)
    {
        readLock();
        try
        {
            return this.items.containsValue(shipItem);
        }
        finally
        {
            readUnlock();
        }
    }
    public Optional<ItemCountable> hasItemCountable(final ItemCountable itemCountable)
    {
        return hasItemCountable(itemCountable.getCardGuid());
    }
    public Optional<ItemCountable> hasItemCountable(final long guid)
    {
        final Optional<ShipItem> optShipItem = getByGUID(guid);
        if (optShipItem.isEmpty())
            return Optional.empty();
        if (optShipItem.get() instanceof ItemCountable itemCountable)
            return Optional.of(itemCountable);
        return Optional.empty();
    }
    public Optional<ShipItem> getByGUID(final long guid)
    {
        readLock();
        try
        {
            return this.items.values().stream()
                    .filter(item -> item.getCardGuid() == guid)
                    .findAny();
        }
        finally
        {
            readUnlock();
        }
    }

    public Set<Integer> getAllItemsIDs()
    {
        readLock();
        try
        {
            return this.items.keySet();
        }
        finally
        {
            readUnlock();
        }
    }




    public List<ShipItem> getAllShipItems()
    {
        return this.items.values().stream().toList();
    }

    @Override
    public ShipItem getByID(final int id)
    {
        readWriteLock.readLock().lock();
        try
        {
            return this.items.get(id);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }

    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        readWriteLock.readLock().lock();
        try
        {
            bw.writeLength(this.items.size());
            for (final ShipItem value : items.values())
            {
                bw.writeDesc(value);
            }
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }
}
