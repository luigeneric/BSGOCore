package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Algorithm3D;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ShipAbilityAffect;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BuffAction extends AbilityAction
{

    public BuffAction(final Ship castingShip,
                      final ShipSlot castingSlot,
                      final List<SpaceObject> targetSpaceObjects,
                      final boolean isAutoCastAbility,
                      final SectorContext ctx
    )
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
    }

    @Override
    protected boolean internalProcess()
    {
        if (ability.getShipAbilityCard().getShipAbilityAffect() == ShipAbilityAffect.Ignore)
            targetSpaceObjects.add(this.castingShip);

        final Vector3 startPos = this.castingShip.getMovementController().getPosition();

        for (final SpaceObject targetSpaceObject : this.targetSpaceObjects)
        {
            final boolean isInRange = Algorithm3D.isInsideRange(startPos, targetSpaceObject.getMovementController().getPosition(),
                    0, ability.getItemBuffAdd().getStatOrDefault(ObjectStat.MaxRange));
            if (isInRange)
            {
                targetSpaceObject.getSpaceSubscribeInfo().addModifier(ShipModifier.create(ability, system, startTimeStamp, castingShip.getPlayerId()));
            }
            else
            {
                if (castingShip.isPlayer())
                {
                    final float distance = Vector3.distance(startPos, targetSpaceObject.getMovementController().getPosition());
                    final float maxRange = ability.getItemBuffAdd().getStatOrDefault(ObjectStat.MaxRange);
                    final float minRange = ability.getItemBuffAdd().getStatOrDefault(ObjectStat.MinRange);

                    final float delta = distance - maxRange;
                    if (delta > 300)
                    {
                        log.warn("Cheat, player {} tier {} used buff {} on something outside his range distance {} min: {} max: {}",
                                castingShip.getPlayerId(),
                                castingShip.getShipCard().getTier(),
                                ability.getShipAbilityCard().getCardGuid(),
                                distance,
                                minRange,
                                maxRange
                        );
                    }
                }
            }

        }
        return true;
    }
}
