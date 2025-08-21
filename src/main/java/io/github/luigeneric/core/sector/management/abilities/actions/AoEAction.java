package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.WeaponFxType;

import java.util.List;

public abstract class AoEAction extends WeaponAction
{
    public AoEAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                     boolean isAutoCastAbility, SectorContext ctx, WeaponFxType weaponFxType, SectorAlgorithms sectorAlgorithms, DamageMediator damageMediator)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx,
                weaponFxType, sectorAlgorithms, damageMediator);
    }

    protected boolean dealAoEDmg()
    {
        final int spotHash = this.getSpotHash();
        for (final SpaceObject target : this.targetSpaceObjects)
        {
            if (target.isRemoved())
                continue;

            final boolean isShipWeaponInRange = this.isShipWeaponInRange(target);
            if (!isShipWeaponInRange)
                continue;

            final boolean isHitByWeapon = this.isHitByWeapon(target);
            if (!isHitByWeapon)
                continue;

            this.damageMediator.dealDamageFromAbility(castingShip, target, ability);
        }

        this.sendWeaponShot(spotHash, null);

        return true;
    }
}
