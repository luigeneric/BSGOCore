package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.utils.ObjectStat;

import java.util.List;

public class RestoreBuffAction extends AbilityAction
{

    public RestoreBuffAction(final Ship castingShip, final ShipSlot castingSlot,
                             final List<SpaceObject> targetSpaceObjects,
                             final boolean isAutoCastAbility, final SectorContext ctx)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
    }

    @Override
    protected boolean internalProcess()
    {
        final float maxHP = casterStats.getStat(ObjectStat.MaxHullPoints);
        final float maxPP = casterStats.getStat(ObjectStat.MaxPowerPoints);
        final float currentHP = casterStats.getHp();
        final float currentPP = casterStats.getPp();


        if (ability.getItemBuffAdd().containsStat(ObjectStat.HullPointRestore))
        {
            final float hpRestore = ability.getItemBuffAdd().getStat(ObjectStat.HullPointRestore);
            final float newHP = Mathf.min(maxHP, currentHP + hpRestore);
            casterStats.setHp(newHP);
        }
        if (ability.getItemBuffAdd().containsStat(ObjectStat.PowerPointRestore))
        {
            final float ppRestore = ability.getItemBuffAdd().getStat(ObjectStat.PowerPointRestore);
            final float newPP = Mathf.min(maxPP, currentPP + ppRestore);
            casterStats.setPp(newPP);
        }
        return true;
    }
}
