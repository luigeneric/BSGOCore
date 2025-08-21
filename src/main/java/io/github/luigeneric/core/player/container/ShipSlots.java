package io.github.luigeneric.core.player.container;


import io.github.luigeneric.templates.utils.ShipSlotType;

import java.util.*;

public class ShipSlots
{
    private final Map<Integer, ShipSlot> slots;

    public ShipSlots(final Map<Integer, ShipSlot> slots)
    {
        this.slots = slots;
    }
    public ShipSlots()
    {
        this(new HashMap<>());
    }

    public void addSlot(final ShipSlot shipSlot)
    {
        this.slots.put(shipSlot.getShipSystem().getServerID(), shipSlot);
    }

    public ShipSlot getSlot(final int slotID)
    {
        return this.slots.get(slotID);
    }

    public Optional<ShipSlot> getPaintSlot()
    {
        return this.slots.values().stream()
                .filter(slot -> slot.getShipSystem() != null)
                .filter(slot -> slot.getShipSystem().getShipSystemCard() != null)
                .filter(slot -> slot.getShipSystem().getShipSystemCard().getShipSlotType() == ShipSlotType.ship_paint)
                .findAny();
    }

    public Optional<ShipSlot> getAvionicSlot()
    {
        return this.slots.values().stream()
                .filter(slot -> slot.getShipSystem() != null)
                .filter(slot -> slot.getShipSystem().getShipSystemCard() != null)
                .filter(slot -> slot.getShipSystem().getShipSystemCard().getShipSlotType() == ShipSlotType.avionics)
                .findAny();
    }

    public Collection<ShipSlot> values()
    {
        return this.slots.values();
    }
    public Set<Map.Entry<Integer, ShipSlot>> entrySet()
    {
        return this.slots.entrySet();
    }
}
