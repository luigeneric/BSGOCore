package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.GalaxyBonus;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Outpost;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OutpostHpBonusTimer extends DelayedTimer
{
    private final GalaxyBonus galaxyBonus;
    public OutpostHpBonusTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks,
                               final GalaxyBonus galaxyBonus)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.galaxyBonus = galaxyBonus;
    }

    @Override
    protected void delayedUpdate()
    {
        final float coloOpBonus = galaxyBonus.getColoOpBonus();
        final float cyloOpBonus = galaxyBonus.getCyloOpBonus();

        final List<Outpost> outposts = sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Outpost);
        for (final Outpost outpost : outposts)
        {
            final float bonusToUse = outpost.getFaction() == Faction.Colonial ? coloOpBonus : cyloOpBonus;
            final SpaceSubscribeInfo statsInfo = outpost.getSpaceSubscribeInfo();
            //log.debug("setting op bonus " + bonusToUse);
            final ObjectStats modStats = statsInfo.getModifiedForStatsBuff();
            final float old = modStats.getStatOrDefault(ObjectStat.MaxHullPoints);
            if (old == bonusToUse)
                continue;

            modStats.setStat(ObjectStat.MaxHullPoints, bonusToUse);
            statsInfo.applyStats();
        }
    }
}
