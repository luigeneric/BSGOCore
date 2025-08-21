package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;

import java.util.Collection;
import java.util.List;

public class CombatTimer extends DelayedTimer
{
    private final long combatTimer;

    public CombatTimer(Tick tick, SectorSpaceObjects sectorSpaceObjects, long delayedTicks, final long combatTimer)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.combatTimer = combatTimer;
    }

    @Override
    protected void delayedUpdate()
    {
        final long currentTimeStamp = this.tick.getTimeStamp();
        final List<SpaceObject> allShips = this.sectorSpaceObjects.getSpaceObjectsOfTypeShip();
        final Collection<Missile> allMissiles = this.sectorSpaceObjects.getSpaceObjectsCollectionOfEntityType(SpaceEntityType.Missile);

        for (final SpaceObject ship : allShips)
        {
            final boolean hasAnyMissile = allMissiles.stream()
                    .filter(m -> m.getMissileLaunchedOnObject() != null)
                    .anyMatch(missile -> missile.getMissileLaunchedOnObject().getObjectID() == ship.getObjectID());
            ship.getSpaceSubscribeInfo().updateCombatStatus(combatTimer, currentTimeStamp, hasAnyMissile);
        }
    }
}
