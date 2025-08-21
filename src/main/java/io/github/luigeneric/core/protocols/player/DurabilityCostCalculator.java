package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.GlobalCard;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.ShipSlotType;

public class DurabilityCostCalculator
{
    private final GlobalCard globalCard;
    public DurabilityCostCalculator(final GlobalCard globalCard)
    {
        this.globalCard = globalCard;
    }

    private float getDeltaDurability(final ShipSystem system)
    {
        return system.getShipSystemCard().getDurability() * (1f - system.quality());
    }
    public float getCostOfSystem(final ShipSystem system, final boolean useCubits) throws IllegalArgumentException
    {
        //no system installed, ignore
        if (system.getCardGuid() == 0)
        {
            throw new IllegalArgumentException("System repair without associated system triggered, client modifications");
        }
        final float repairMult = globalCard.getRepairCard(useCubits);
        return Mathf.ceil(getDeltaDurability(system) * repairMult);
    }

    public float getShipHullRepairCosts(final HangarShip ship, final boolean useCubits)
    {
        final float repairMult = globalCard.getRepairCard(useCubits);
        return Mathf.ceil(repairMult * (ship.getShipCard().getDurability() - ship.getDurability()));
    }
    public long getRepairAllCosts(final HangarShip ship, final boolean useCubits)
    {
        final float durabilityMult = globalCard.getRepairCard(useCubits);

        float totalCosts = 0;
        //use titanium
        if (!useCubits)
        {
            totalCosts += ship.getShipCard().getDurability() * (1f - ship.quality()) * durabilityMult;
        }
        for (final ShipSlot slot : ship.getShipSlots().values())
        {
            if (slot.getShipSlotCard().getShipSlotType() == ShipSlotType.ship_paint)
                continue;

            if (slot.getShipSystem() != null && slot.getShipSystem().getCardGuid() != 0)
            {
                totalCosts += this.getCostOfSystem(slot.getShipSystem(), useCubits);
            }
        }
        return Math.round(totalCosts);
    }

    public void repairSystem(final ShipSlot shipSlot)
    {
        repairSystem(shipSlot.getShipSystem());
    }

    public void repairSystem(final ShipSystem shipSystem)
    {
        if (shipSystem == null || shipSystem.getCardGuid() == 0)
        {
            return;
        }
        if (shipSystem.getShipSystemCard().getShipSlotType() == ShipSlotType.avionics ||
        shipSystem.getShipSystemCard().getShipSlotType() == ShipSlotType.ship_paint)
        {
            return;
        }
        shipSystem.setDurabilityToMax();
    }
}
