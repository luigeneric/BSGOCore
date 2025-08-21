package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpaceObjectPropertiesTimer extends DelayedTimer
{
    public SpaceObjectPropertiesTimer(final Tick tick, SectorSpaceObjects sectorSpaceObjects, final long delayedTicks)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
    }

    @Override
    protected void delayedUpdate()
    {
        for (final SpaceObject spaceObject : this.sectorSpaceObjects.values())
        {
            try
            {
                final SpaceSubscribeInfo spaceSubscribeInfo = spaceObject.getSpaceSubscribeInfo();
                spaceSubscribeInfo.sendSpacePropertyBuffer();
            }
            catch (Exception ex)
            {
                log.error("Unknown error in delayed update for send space subscribe info; spaceObject={}", spaceObject);
            }
        }
    }
}
