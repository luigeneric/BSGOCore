package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ScheduleItem;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.spawn.SpawnAble;
import io.github.luigeneric.core.sector.management.spawn.SpawnController;

import java.util.List;

public class SpawnScheduler extends UpdateTimer
{
    private final SpawnController spawnController;
    private final Tick tick;
    public SpawnScheduler(final SectorSpaceObjects sectorSpaceObjects, SpawnController spawnController, final Tick tick)
    {
        super(sectorSpaceObjects);
        this.spawnController = spawnController;
        this.tick = tick;
    }

    @Override
    public void update(float dt)
    {
        final List<ScheduleItem<SpawnAble>> allReadyItems = spawnController.getAllTimeoutItems(this.tick);
        for (final ScheduleItem<SpawnAble> scheduleItem : allReadyItems)
        {
            scheduleItem.getEntry().spawn();
        }
    }
}
