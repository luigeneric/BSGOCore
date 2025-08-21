package io.github.luigeneric.core.spaceentities.statsinfo.buffer;


import io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates.PropertyUpdate;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.Owner;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.StatsProtocolSubscriber;

public class PlayerPropertyBuffer extends BasePropertyBuffer
{
    public PlayerPropertyBuffer(final Owner owner, final StatsProtocolSubscriber statsProtocolSubscriber)
    {
        super(owner, statsProtocolSubscriber);
    }

    @Override
    protected boolean isPropertyUpdateAllowed(PropertyUpdate propertyUpdate)
    {
        return true;
    }
}
