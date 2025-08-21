package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JumpInTimer extends DelayedTimer
{
    private final Map<Long, Long> jumpingInShips;
    public JumpInTimer(final SectorContext ctx, long delayedTicks)
    {
        super(ctx.tick(), ctx.spaceObjects(), delayedTicks);
        this.jumpingInShips = new ConcurrentHashMap<>();
    }

    @Override
    protected void delayedUpdate()
    {
        final long currentTimeStamp = tick.getTimeStamp();
        for (Map.Entry<Long, Long> jumpIns : this.jumpingInShips.entrySet())
        {
            final long objectID = jumpIns.getKey();
            final long joinTimeStamp = jumpIns.getValue();
            final long timeUp = joinTimeStamp + 1000 * 10;
            final long delta = currentTimeStamp - timeUp;
            if (delta < 0) continue;
            this.jumpingInShips.remove(objectID);
        }
    }
}
