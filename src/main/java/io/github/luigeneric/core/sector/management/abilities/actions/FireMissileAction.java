package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.movement.maneuver.TargetLaunchManeuver;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.enums.WeaponFxType;
import io.github.luigeneric.templates.cards.ShipConsumableCard;
import io.github.luigeneric.templates.shipitems.ShipConsumable;
import io.github.luigeneric.templates.utils.ConsumableEffectType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.ShipConsumableOption;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
public class FireMissileAction extends WeaponAction
{
    private final SectorJoinQueue sectorJoinQueue;
    public FireMissileAction(final Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                             boolean isAutoCastAbility, SectorContext ctx, SectorAlgorithms sectorAlgorithms,
                             DamageMediator damageMediator, final SectorJoinQueue joinQueue)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx,
                WeaponFxType.MissileLauncher, sectorAlgorithms, damageMediator);
        this.sectorJoinQueue = joinQueue;
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

        final boolean useConsumable = ability.getShipAbilityCard().getShipConsumableOption() == ShipConsumableOption.Using;
        final long missileGUID = getMissileGUID(useConsumable);
        if (missileGUID == -1)
            return false;

        final SpaceObject missile = this.ctx.spaceObjectFactory()
                .createMissile(castingShip, target, spotDesc, missileGUID, startTimeStamp);

        final ObjectStats missileStats = missile.getSpaceSubscribeInfo().getStats();
        missileStats.put(ability.getItemBuffAdd());
        missile.getMovementController().getMovementOptions().setGear(Gear.Regular);
        missile.getMovementController().getMovementOptions().setThrottleSpeed(missileStats.getStat(ObjectStat.Speed));
        missileStats.setStat(ObjectStat.MaxPowerPoints, 0);
        missile.getSpaceSubscribeInfo().setHp(missileStats.getStat(ObjectStat.MaxHullPoints));
        missile.getSpaceSubscribeInfo().setMaxHpPp();

        final boolean gearIsSlide = castingShip.getMovementController().getMovementOptions().getGear() == Gear.RCS;
        final float relativeSpeed = gearIsSlide ? castingShip.getMovementController().getFrame().getLinearSpeed().magnitude() * 0.5f : 0;
        final TargetLaunchManeuver targetLaunchManeuver =
                new TargetLaunchManeuver(castingShip, spotDesc, relativeSpeed, target);
        missile.getMovementController().setNextManeuver(targetLaunchManeuver);

        this.sectorJoinQueue.addSpaceObject(missile);
        sendWeaponShot(this.getSpotHash(), target);

        return true;
    }

    private long getMissileGUID(final boolean useConsumable)
    {
        final ShipConsumable currentConsumable = castingSlot.getCurrentConsumable();
        final ShipConsumableCard shipConsumableCard = currentConsumable.getShipConsumableCard();
        if (shipConsumableCard == null && useConsumable)
        {
            log.error("FireMissileAction: ShipConsumableCard for missile was null");
            return -1;
        }


        //This must be the biggest shitcode I've ever wrote because I cant find first fucking property into these cards that tells me if its mini
        // or normal nuke
        long missileGUID = 0;
        if (!useConsumable)
        {
            missileGUID = StaticCardGUID.MissileCard.getValue();
        }
        else
        {
            if (Objects.requireNonNull(shipConsumableCard.getEffectType()) == ConsumableEffectType.DamageNuclear)
            {
                final float dmgHigh = shipConsumableCard.getItemBuffAdd().getStat(ObjectStat.DamageHigh);
                if (dmgHigh == 4.0)
                {
                    missileGUID = StaticCardGUID.MissileMiniNuke.getValue();
                }
                if (dmgHigh == 19.0)
                {
                    missileGUID = StaticCardGUID.MissileNuke.getValue();
                }
            } else
            {
                missileGUID = StaticCardGUID.MissileCard.getValue();
            }
        }

        return missileGUID;
    }
}


