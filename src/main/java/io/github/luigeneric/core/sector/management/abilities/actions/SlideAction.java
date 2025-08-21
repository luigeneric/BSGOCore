package io.github.luigeneric.core.sector.management.abilities.actions;



import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementOptions;
import io.github.luigeneric.core.movement.maneuver.TurnByPitchYawStrikes;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.linearalgebra.base.Vector2;

import java.util.List;

public class SlideAction extends AbilityAction
{

    public SlideAction(Ship castingShip, ShipSlot castingSlot,
                       List<SpaceObject> targetSpaceObjects,
                       boolean isAutoCastAbility, SectorContext ctx)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
    }

    @Override
    protected boolean internalProcess()
    {
        final ShipModifier slide = ShipModifier.create(ability, system, startTimeStamp, castingShip.getPlayerId());
        casterStats.addModifier(slide);
        final MovementOptions options = castingShip.getMovementController().getMovementOptions();
        final Maneuver currentManeuver = castingShip.getMovementController().getCurrentManeuver();
        if (currentManeuver instanceof TurnByPitchYawStrikes turnByPitchYawStrikesOld)
        {
            final Vector2 newStrafeValues = turnByPitchYawStrikesOld.getStrafeDirection().copy();
            newStrafeValues.setX(newStrafeValues.getX() * 0f);
            newStrafeValues.setY(newStrafeValues.getY() * 0f);
            final TurnByPitchYawStrikes turnByPitchYawStrikesRCSEdition =
                    new TurnByPitchYawStrikes(
                            turnByPitchYawStrikesOld.getPitchYawRollFactor().copy(),
                            newStrafeValues,
                            turnByPitchYawStrikesOld.getStrafeMagnitude()
                    );
            castingShip.getMovementController().setNextManeuver(turnByPitchYawStrikesRCSEdition);
        }
        options.setGear(Gear.RCS);
        castingShip.getMovementController().setMovementOptionsNeedUpdate();
        return true;
    }
}

