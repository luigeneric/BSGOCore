package io.github.luigeneric.core.player;


import com.google.gson.annotations.Expose;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.player.container.containerids.ShipSlotContainerID;
import io.github.luigeneric.core.spaceentities.bindings.StickerBinding;
import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.PlayerSubscribeInfo;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.*;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HangarShip
{
    /**
     * ushort
     */
    private final int serverId;

    @Expose(serialize = false)
    private final ShopItemCard shopItemCard;
    @Expose(serialize = false)
    private final ShipCard shipCard;
    @Expose(serialize = false)
    private final WorldCard worldCard;
    @Expose(serialize = false)
    private final OwnerCard ownerCard;
    private final ShipAspects shipAspects;
    private float durability;

    private String name;
    /**
     * ushort, ShipSlot
     */
    private final ShipSlots shipSlots;
    private List<StickerBinding> stickers;

    @Expose(serialize = false)
    private final SpaceSubscribeInfo shipStats;


    public HangarShip(final long userID, final int serverId, final long guid, final String name)
    {
        Catalogue catalogue = CDI.current().select(Catalogue.class).get();
        if (serverId < 0) throw new IllegalArgumentException("serverId cannot be less than 0");
        if (guid < 0) throw new IllegalArgumentException("guid cannot be less than 0");
        Objects.requireNonNull(name, "Name for hangar-ship cannot be null");

        this.serverId = serverId;
        this.name = name;
        this.shipAspects = new ShipAspects();

        final Optional<ShopItemCard> optShopItemCard = catalogue.fetchCard(guid, CardView.Price);
        final Optional<ShipCard> optShipCard = catalogue.fetchCard(guid, CardView.Ship);
        final Optional<WorldCard> optWorldCard = catalogue.fetchCard(guid, CardView.World);
        final Optional<OwnerCard> optOwnerCard = catalogue.fetchCard(guid, CardView.Owner);


        if (optShopItemCard.isEmpty()) throw new IllegalArgumentException("Could not find shopItemCard");
        if (optShipCard.isEmpty()) throw new IllegalArgumentException("Could not find ShipCard");
        if (optWorldCard.isEmpty()) throw new IllegalArgumentException("Could not find WorldCard");
        if (optOwnerCard.isEmpty()) throw new IllegalArgumentException("Could not find OwnerCard");

        this.shopItemCard = optShopItemCard.get();
        this.shipCard = optShipCard.get();
        this.worldCard = optWorldCard.get();
        this.ownerCard = optOwnerCard.get();

        this.shipSlots = new ShipSlots();
        this.stickers = new ArrayList<>();

        this.setupFromCardsAndParams();
        //this.shipStats = new SpaceSubscribeInfo(userID, this.shipCard.getStats());
        this.shipStats = new PlayerSubscribeInfo(userID, this.shipCard.getStats());
        this.shipStats.setShipSlots(shipSlots);
    }

    private void setupFromCardsAndParams()
    {
        //set to max durability
        this.durability = this.shipCard.getDurability();

        final ShipSlotCard[] shipSlotCards = this.shipCard.getShipSlotCards();
        for (final ShipSlotCard shipSlotCard : shipSlotCards)
        {
            var slotID = shipSlotCard.getSlotId();
            var slotType = shipSlotCard.getShipSlotType();
            var slotLevel = shipSlotCard.getLevel();
            ShipSlot shipSlot = new ShipSlot(new ShipSlotContainerID(serverId, slotID), shipSlotCard);
            if (shipCard.getLevel() >= slotLevel)
            {
                //this.shipSlots.put(slotID, shipSlot);
                this.shipSlots.addSlot(shipSlot);
            }
        }
    }

    public synchronized void setSlots(final ShipSlots slots)
    {
        slots.values().forEach(this.shipSlots::addSlot);
        this.shipStats.applyStats();
    }

    public synchronized ShipSlots getShipSlots()
    {
        return shipSlots;
    }

    public int getServerId()
    {
        return serverId;
    }

    public long getCardGuid()
    {
        return this.shipCard.getCardGuid();
    }

    public ShopItemCard getShopItemCard()
    {
        return shopItemCard;
    }

    public ShipCard getShipCard()
    {
        return shipCard;
    }

    public synchronized float getDurability()
    {
        if (this.durability < 0)
        {
            return this.shipCard.getDurability();
        }
        return durability;
    }

    public List<StickerBinding> getStickers()
    {
        return stickers;
    }

    public synchronized void addSticker(StickerBinding stickerBinding)
    {
        this.stickers.add(stickerBinding);
    }

    public synchronized void removeSticker(StickerBinding sticker)
    {
        this.stickers.remove(sticker);
    }

    public synchronized String getName()
    {
        return name;
    }

    public synchronized void setName(String name)
    {
        this.name = name;
    }

    public synchronized SpaceSubscribeInfo getShipStats()
    {
        return shipStats;
    }

    public WorldCard getWorldCard()
    {
        return worldCard;
    }

    public OwnerCard getOwnerCard()
    {
        return ownerCard;
    }

    public ShipAspects getShipAspects()
    {
        return shipAspects;
    }


    public synchronized float quality()
    {
        if (this.shipCard.getDurability() == 0)
            throw new IllegalStateException("ShipCard durability was 0!!!");

        final float cardDurabilityCleaned = shipCard.getDurability() <= 0 ? 1f : shipCard.getDurability();
        return this.getDurability() / cardDurabilityCleaned;
    }
    private synchronized float getDeltaDurability()
    {
        return this.shipCard.getDurability() * (1f - quality());
    }
    public float getRepairCosts(final boolean useCubits)
    {
        final Catalogue catalogue = CDI.current().select(Catalogue.class).get();
        final GlobalCard globalCard = catalogue.fetchCardUnsafe(StaticCardGUID.GlobalCard, CardView.Global);
        final float repairMultiplier = globalCard.getRepairCard(useCubits);
        return Mathf.ceil(getDeltaDurability() * repairMultiplier);
    }

    public synchronized void reduceDurability(final float decrementValue) throws IllegalArgumentException
    {
        if (this.durability == 0)
            return;

        setDurability(this.durability - decrementValue);
    }

    public void setDurability(final float durability)
    {
        this.durability = Mathf.clampSafe(durability, 0, this.shipCard.getDurability());
    }

    public void setDurabilityToMax()
    {
        this.durability = this.shipCard.getDurability();
    }

    @Override
    public String toString()
    {
        return "HangarShip{" +
                "serverId=" + serverId +
                ", shopItemCard=" + shopItemCard +
                ", shipCard=" + shipCard +
                ", shipAspects=" + shipAspects +
                ", durability=" + durability +
                ", name='" + name + '\'' +
                ", shipSlots=" + shipSlots +
                ", stickers=" + stickers +
                ", shipStats=" + shipStats +
                '}';
    }
}
