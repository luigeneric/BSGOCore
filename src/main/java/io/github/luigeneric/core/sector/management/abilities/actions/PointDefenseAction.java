package io.github.luigeneric.core.sector.management.abilities.actions;


import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.WeaponFxType;

import java.util.List;

public class PointDefenseAction extends AoEAction
{
    public PointDefenseAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                              boolean isAutoCastAbility, SectorContext ctx,
                              SectorAlgorithms sectorAlgorithms, DamageMediator damageMediator)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx,
                WeaponFxType.PointDefence, sectorAlgorithms, damageMediator);
    }

    @Override
    protected boolean internalProcess()
    {
        return this.dealAoEDmg();
    }
}
