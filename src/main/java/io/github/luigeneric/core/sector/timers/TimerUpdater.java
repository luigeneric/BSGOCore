package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.SectorJob;
import io.github.luigeneric.core.sector.Tick;

import java.util.List;

public class TimerUpdater implements SectorJob
{
    private final Tick tick;
    private final List<UpdateTimer> timers;

    public TimerUpdater(final Tick tick, final List<UpdateTimer> timers)
    {
        this.tick = tick;
        this.timers = timers;
    }

    /**
     * UpdateTimers updates each timer if needed. There are timers which doesn't require an update such as DelayedTimers
     * Example Values:
     *  whith first value of 1 second (1 second ==> 1 update each 10 ticks=
     * //@throws IOException if one of the timers calls ioexception
     */
    @Override
    public void run()
    {
        final float dt = tick.getDeltaTime();
        for (final UpdateTimer timer : this.timers)
        {
            timer.update(dt);
        }
    }
}
