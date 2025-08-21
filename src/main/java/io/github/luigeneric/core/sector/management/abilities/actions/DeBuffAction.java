package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.gameplayalgorithms.IEWDurationAlgorithm;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Algorithm3D;
import io.github.luigeneric.templates.utils.ObjectStat;

import java.util.List;

public class DeBuffAction extends AbilityAction
{
    protected final IEWDurationAlgorithm ewDurationAlgo;

    public DeBuffAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                        boolean isAutoCastAbility, final SectorContext ctx,
                        final IEWDurationAlgorithm ewDurationAlgo)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
        this.ewDurationAlgo = ewDurationAlgo;
    }


    @Override
    protected boolean internalProcess()
    {
        final float standardDuration = ability.getItemBuffAdd().getStatOrDefault(ObjectStat.Duration);
        final float emitterRate = this.casterStats.getStat(ObjectStat.PenetrationStrength);

        final Vector3 startPos = this.castingShip.getMovementController().getPosition();

        for (final SpaceObject targetSpaceObject : this.targetSpaceObjects)
        {
            final boolean isInRange = Algorithm3D.isInsideRange(startPos, targetSpaceObject.getMovementController().getPosition(),
                    0, ability.getItemBuffAdd().getStatOrDefault(ObjectStat.MaxRange));
            final float fireWall = targetSpaceObject.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.FirewallRating);
            if (isInRange)
            {
                final ShipModifier modifier = ShipModifier.create(ability, system, startTimeStamp, castingShip.getPlayerId());
                final float cleanedDuration = this.ewDurationAlgo.getHackDuration(standardDuration, emitterRate, fireWall);
                modifier.getItemBuffAdd().setStat(ObjectStat.Duration, cleanedDuration);
                targetSpaceObject.getSpaceSubscribeInfo().addModifier(modifier);
            }
        }

        if (castingShip.isPlayer())
        {
            castingShip.getSpaceSubscribeInfo().setLastCombatTime(ctx.tick().getTimeStamp());
        }

        return true;
    }
}
