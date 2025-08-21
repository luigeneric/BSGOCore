package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.ShipModifiers;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Computer for Vanir/Hel
 */
@Slf4j
public class RecoverComputerBuffTimer extends DelayedTimer
{
    public RecoverComputerBuffTimer(Tick tick, SectorSpaceObjects sectorSpaceObjects, long delayedTicks)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
    }

    @Override
    protected void delayedUpdate()
    {
        for (final SpaceObject spaceObject : this.sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Player))
        {
            try
            {
                final SpaceSubscribeInfo stats = spaceObject.getSpaceSubscribeInfo();
                final Optional<ShipModifiers> optModifier = stats.getModifiers();
                if (optModifier.isEmpty())
                    continue;

                final Float maxHp = stats.getStat(ObjectStat.MaxHullPoints);
                if (maxHp == null)
                {
                    log.error("Unexpected error, maxHP was null inside RecoveryComputerBuffTimer");
                    return;
                }

                final ShipModifiers modifiers = optModifier.get();
                //log.info("modifier found: " + modifiers);
                modifiers.filterForBestRemoteBuffAdd()
                        .entrySet()
                        .stream()
                        .filter(stat -> stat.getKey() == ObjectStat.HullRecovery)
                        .forEach(stat ->
                {
                    final float newValue = stats.getHp() + stat.getValue();
                    final float newHp = Mathf.clampSafe(newValue, 0, maxHp);
                    stats.setHp(newHp);
                });
            }
            catch (Exception exception)
            {
                log.error("Unexpected exception in RecoverComputer timer", exception);
            }
        }
    }
}
