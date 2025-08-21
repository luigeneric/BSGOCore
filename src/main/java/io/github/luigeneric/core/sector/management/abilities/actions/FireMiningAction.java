package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.WeaponFxType;

import java.util.List;

public class FireMiningAction extends WeaponAction
{
    public FireMiningAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                               boolean isAutoCastAbility, SectorContext ctx, SectorAlgorithms sectorAlgorithms,
                               DamageMediator damageMediator)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx, WeaponFxType.Gun, sectorAlgorithms, damageMediator);
    }

    @Override
    protected boolean internalProcess()
    {
        final boolean isTargetSizeOkay = this.targetSizeSatisfied(1);
        if (!isTargetSizeOkay)
            return false;

        final SpaceObject target = targetSpaceObjects.get(0);
        if (target.isRemoved())
        {
            return false;
        }
        final boolean isShipWeaponInRange = this.isShipWeaponInRange(target);
        if (!isShipWeaponInRange)
        {
            return false;
        }

        final boolean isHit = isHitByWeapon(target);
        if (isHit)
        {
            this.damageMediator.dealDamageFromMining(castingShip, target, ability);
        }
        sendWeaponShot(this.getSpotHash(), target);
        return true;
    }
}

