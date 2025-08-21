package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.core.gameplayalgorithms.IEWDurationAlgorithm;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.List;

public class ActivatePaintTheTargetAction extends DeBuffAction
{
    public ActivatePaintTheTargetAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                                        boolean isAutoCastAbility, final SectorContext ctx,
                                        final IEWDurationAlgorithm ewDurationAlgo)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx, ewDurationAlgo);
    }

    @Override
    protected boolean internalProcess()
    {
        if (targetSpaceObjects.size() != 1)
            return false;

        final boolean result = super.internalProcess();
        if (!result)
            return false;

        //activate paint the target
        final SpaceObject target = targetSpaceObjects.get(0);
        target.getSpaceObjectState().setMarkedByCarrier(true);
        return true;
    }
}
