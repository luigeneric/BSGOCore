package io.github.luigeneric.templates.shipitems;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipSystemCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;

import java.time.*;
import java.util.Objects;
import java.util.Optional;

public class ShipSystem extends ShipItem
{
    private float durability;
    private double timeOfLastUse;
    private ShipSystemCard shipSystemCard;

    private ShipSystem(final long cardGuid, final float durability, final double timeOfLastUse, final int serverID)
    {
        super(cardGuid, ItemType.System, serverID);
        this.durability = durability;
        this.timeOfLastUse = timeOfLastUse;
    }

    /**
     * Creates a new ShipSystem using a cardGUID and fetches the associated ShipSystemCard
     * @param cardGUID the id to be used to fetch the ShipSystemCard
     * @return a new ShipSystem
     */
    public static ShipSystem fromGUID(final long cardGUID) throws IllegalArgumentException
    {
        final ShipSystem system = new ShipSystem(cardGUID, Float.MAX_VALUE, 0, 0);
        final Optional<ShipSystemCard> optCard = CDI.current().select(Catalogue.class).get().fetchCard(cardGUID, CardView.ShipSystem);
        final ShipSystemCard tmpCard = optCard.orElseThrow(() -> new IllegalArgumentException("card for guid was null! " + cardGUID));
        system.setShipSystemCard(tmpCard);
        system.setDurability(system.shipSystemCard.getDurability());
        return system;
    }

    /**
     * Creates a new ShipSystem with 0 durability, 0 timeOfLastUse and no associated CardGUID
     * WARNING, only use if you need a dummy
     * @param serverID
     * @return
     */
    public static ShipSystem fromServerId(final int serverID)
    {
        return new ShipSystem(serverID);
    }

    /**
     * Creates an empty shipsystem
     */
    private ShipSystem(final int serverID)
    {
        super(0, ItemType.None, serverID);
        this.durability = 0;
        this.timeOfLastUse = 0;
    }

    public void setShipSystemCard(final ShipSystemCard shipSystemCard)
    {
        Objects.requireNonNull(shipSystemCard, "ShipSystemCard cannot be null");
        this.shipSystemCard = shipSystemCard;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        if (this.itemType != ItemType.None)
        {
            bw.writeSingle(durability);
            bw.writeDouble(timeOfLastUse);
        }
    }

    @Override
    public ShipItem copy()
    {
        return fromGUID(this.cardGuid);
    }

    public float getDurability()
    {
        if (this.shipSystemCard != null && shipSystemCard.isIndestructible())
            this.setDurabilityToMax();
        return durability;
    }

    public double getTimeOfLastUse()
    {
        return timeOfLastUse;
    }
    public LocalDateTime getTimeOfLastUseLocalDateTime()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (this.timeOfLastUse * 1000)), ZoneId.of("UTC"));
    }

    public boolean isBroken()
    {
        return this.durability == 0;
    }

    /**
     * set now
     */
    public void setTimeOfLastUse()
    {
        this.setTimeOfLastUse(LocalDateTime.now(Clock.systemUTC()));
    }
    public void setTimeOfLastUse(final LocalDateTime localDateTime)
    {
        this.timeOfLastUse = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli() * 0.001;
    }
    public void setTimeOfLastUse(final long timeStampMs)
    {
        this.timeOfLastUse = timeStampMs * 0.001;
    }

    public ShipSystemCard getShipSystemCard()
    {
        return shipSystemCard;
    }

    @Override
    public String toString()
    {
        return "ShipSystem{" +
                "shipSystemCard=" + shipSystemCard +
                ", itemType=" + itemType +
                '}';
    }

    public void setDurability(final float durability)
    {
        this.durability = Mathf.clampSafe(durability, 0, shipSystemCard.getDurability());
    }
    public void reduceDurability(final float decreaseValue) throws IllegalArgumentException
    {
        if (this.shipSystemCard != null && shipSystemCard.isIndestructible())
            return;

        if (durability == 0)
            return;

        this.setDurability(durability - decreaseValue);
    }


    public float quality()
    {
        if (shipSystemCard != null)
            return this.getDurability() / this.shipSystemCard.getDurability();
        return 1f;
    }

    private float getDeltaDurability()
    {
        return shipSystemCard.getDurability() * (1f - quality());
    }

    public void setDurabilityToMax()
    {
        setDurability(shipSystemCard.getDurability());
    }
}
