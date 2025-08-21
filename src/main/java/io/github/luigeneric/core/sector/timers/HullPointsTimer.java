package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HullPointsTimer extends UpdateTimer
{
    public HullPointsTimer(final SectorSpaceObjects sectorSpaceObjects)
    {
        super(sectorSpaceObjects);
    }

    @Override
    public void update(final float dt)
    {
        for (final SpaceObject spaceObject : this.sectorSpaceObjects.getSpaceObjectsNotOfEntityType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Planetoid,
                SpaceEntityType.Missile,
                SpaceEntityType.Planet))
        {
            if (spaceObject.isRemoved())
                continue;

            final SpaceSubscribeInfo stats = spaceObject.getSpaceSubscribeInfo();
            if (stats.isInCombat())
                continue;

            final Float recoveryHP = stats.getStat(ObjectStat.HullRecovery);
            final Float maxHP = stats.getStat(ObjectStat.MaxHullPoints);
            if (recoveryHP == null || maxHP == null)
                continue;

            final float currentHP = stats.getHp();

            //if the recovery value is lower or equal to 0, there is no point in regeneration (probably later with drain or something but this would be first bad solution)
            if (recoveryHP <= 0)
                continue;

            //if hp is full or lower than 0(which should never happen), there should be no regeneration
            if (currentHP == maxHP || currentHP <= 0)
                continue;

            final float newHP = Mathf.clampSafe(currentHP + recoveryHP * dt, 0, maxHP);
            if (newHP < currentHP)
            {
                stats.capMaxHp();
                continue;
            }
            stats.setHp(newHP);
        }
    }
}
