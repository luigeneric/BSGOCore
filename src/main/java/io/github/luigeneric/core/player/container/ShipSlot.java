package io.github.luigeneric.core.player.container;

import com.google.gson.annotations.Expose;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.ShipAbility;
import io.github.luigeneric.core.player.container.containerids.ShipSlotContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipAbilityCard;
import io.github.luigeneric.templates.cards.ShipSlotCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipConsumable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.ShipSlotType;
import jakarta.enterprise.inject.spi.CDI;
import lombok.Getter;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ShipSlot extends IContainer implements IProtocolWrite
{
    /**
     * if the shipSystem is null it means the slot is not taken
     */
    @Getter
    private ShipSystem shipSystem;
    @Getter
    private ShipConsumable currentConsumable;
    @Getter
    private ShipAbility shipAbility;
    @Getter
    @Expose(serialize = false)
    private final ShipSlotCard shipSlotCard;
    private final Catalogue catalogue;
    private final ReadWriteLock readWriteLock;

    private ShipSlot(final ShipSlotContainerID shipSlotContainerID, final ShipSystem shipSystem, final ItemCountable currentConsumable,
                     final ShipSlotCard shipSlotCard)
    {
        super(shipSlotContainerID);
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.shipSystem = shipSystem;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.currentConsumable = new ShipConsumable(currentConsumable, catalogue.fetchCardUnsafe(currentConsumable.getCardGuid(), CardView.ShipConsumable));


        this.shipSlotCard = shipSlotCard;
    }

    public ShipSlot(final ShipSlotContainerID shipSlotContainerID, final ShipSlotCard shipSlotCard)
    {
        this(shipSlotContainerID, ShipSystem.fromServerId(shipSlotCard.getSlotId()), ItemCountable.placeHolder(), shipSlotCard);
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        readWriteLock.readLock().lock();
        try
        {
            bw.writeDesc(this.shipSystem);
            bw.writeGUID(this.currentConsumable.getItemCountable().getCardGuid());
            bw.writeBoolean(this.isInoperable());
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    private boolean isInoperable()
    {
        if (this.shipSystem == null)
            return false;

        if (shipSystem.getCardGuid() == 0)
            return false;

        if (shipSystem.getShipSystemCard().getShipSlotType() == ShipSlotType.ship_paint)
            return false;

        final float quality = shipSystem.quality();

        //inoperable if quality less than 10%
        return quality < 0.1f;
    }

    @Override
    public String toString()
    {
        return "ShipSlot{" +
                "shipSystem=" + shipSystem +
                ", currentConsumable=" + currentConsumable +
                ", inoperable=" + isInoperable() +
                '}';
    }

    @Override
    public ShipItem getByID(int id)
    {
        return this.shipSystem;
    }

    @Override
    public Set<Integer> getAllItemsIDs()
    {
        readWriteLock.readLock().lock();
        try
        {
            return Collections.singleton(this.shipSystem.getServerID());
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public ShipItem removeShipItem(int itemId)
    {
        readWriteLock.writeLock().lock();
        try
        {
            final ShipSystem tmp = this.shipSystem;
            this.shipSystem = ShipSystem.fromServerId(tmp.getServerID());
            this.currentConsumable = new ShipConsumable(ItemCountable.placeHolder(), null);
            return tmp;
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    public ShipItem removeShipItem()
    {
        return removeShipItem(0);
    }

    public void setCurrentConsumable(final ItemCountable currentConsumable)
    {
        readWriteLock.writeLock().lock();
        try
        {
            this.currentConsumable = new ShipConsumable(currentConsumable, catalogue.fetchCardUnsafe(currentConsumable.getCardGuid(), CardView.ShipConsumable));
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }


    @Override
    public ShipItem addShipItem(final ShipItem shipItem)
    {
        if (!(shipItem instanceof ShipSystem newShipSystem))
        {
            throw new IllegalArgumentException("trying to set something else than ShipSystem on Slot: " + this.shipSystem.getServerID());
        }

        readWriteLock.writeLock().lock();
        try
        {
            final int serverID = this.shipSystem.getServerID();
            this.shipSystem = ShipSystem.fromGUID(newShipSystem.getCardGuid());
            if (this.shipSystem.getShipSystemCard().getShipAbilityCards().length > 0)
            {
                final Optional<ShipAbilityCard> optShipAbilityCard =
                        catalogue.fetchCard(this.shipSystem.getShipSystemCard().getShipAbilityCards()[0], CardView.ShipAbility);
                if (optShipAbilityCard.isEmpty())
                {
                    throw new IllegalStateException("Could not find ShipAbilityCard!");
                }

                final ShipAbilityCard shipAbilityCard = optShipAbilityCard.get();
                this.setShipAbility(shipAbilityCard);
            }
            this.shipSystem.setServerID(serverID);
            this.shipSystem.setDurability(newShipSystem.getDurability());
            return newShipSystem;
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    public void setShipAbility(final ShipAbilityCard shipAbilityCard)
    {
        readWriteLock.writeLock().lock();
        try
        {
            this.shipAbility = new ShipAbility(shipAbilityCard);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void accept(final ContainerVisitor containerVisitor)
    {
        containerVisitor.visit(this);
    }
}
