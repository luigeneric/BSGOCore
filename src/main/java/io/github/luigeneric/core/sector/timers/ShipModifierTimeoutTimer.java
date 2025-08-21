package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.movement.MovementOptions;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.ShipModifiers;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.utils.AbilityActionType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShipModifierTimeoutTimer extends UpdateTimer
{
    public ShipModifierTimeoutTimer(final SectorSpaceObjects sectorSpaceObjects)
    {
        super(sectorSpaceObjects);
    }

    @Override
    public void update(final float dt)
    {
        final List<SpaceObject> tmpObjects = this.sectorSpaceObjects
                .getSpaceObjectsNotOfEntityType(SpaceEntityType.Asteroid, SpaceEntityType.Planetoid, SpaceEntityType.Missile);


        for (final SpaceObject spaceObject : tmpObjects)
        {
            final Optional<ShipModifiers> optModifiers = spaceObject.getSpaceSubscribeInfo().getModifiers();
            if (optModifiers.isEmpty()) continue;

            final ShipModifiers modifiers = optModifiers.get();
            Set<Long> timeOutModifiers = modifiers.checkTimeout();

            final List<Long> slides = modifiers.getOfType(AbilityActionType.Slide)
                    .stream()
                    .map(ShipModifier::getServerID)
                    .toList();

            if (!timeOutModifiers.isEmpty())
            {
                final boolean containsAll = timeOutModifiers.containsAll(slides);
                final MovementOptions currentMovementOpts = spaceObject.getMovementController().getMovementOptions();
                final Gear currentGear = currentMovementOpts.getGear();
                if (containsAll && !slides.isEmpty() && currentGear == Gear.RCS)
                {
                    final Gear lastGear = currentMovementOpts.getLastGear();
                    currentMovementOpts.setGear(lastGear);
                }


                spaceObject.getSpaceSubscribeInfo().removeModifiers(timeOutModifiers);
            }

            //there is no more paint the target computer
            if (
                    spaceObject.getSpaceObjectState().getIsMarkedByCarrier() &&
                    modifiers.getOfTypeStream(AbilityActionType.ActivatePaintTheTarget).findAny().isEmpty()
            )
            {
                spaceObject.getSpaceObjectState().setMarkedByCarrier(false);
            }
        }
    }
}
