package io.github.luigeneric.core.spaceentities.statsinfo.buffer;


import io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates.ObjectStatUpdate;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates.PropertyUpdate;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.Owner;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.StatsProtocolSubscriber;
import io.github.luigeneric.templates.utils.ObjectStat;

public class SpacePropertyBuffer extends BasePropertyBuffer
{
    public SpacePropertyBuffer(Owner owner, StatsProtocolSubscriber statsProtocolSubscriber)
    {
        super(owner, statsProtocolSubscriber);
    }

    @Override
    protected boolean isPropertyUpdateAllowed(PropertyUpdate propertyUpdate)
    {
        boolean rv = false;
        switch (propertyUpdate.getSpaceUpdateType())
        {
            case HullPoints, PowerPoints -> rv = true;
            case ObjectStat -> {
                ObjectStatUpdate objectStatUpdate = (ObjectStatUpdate) propertyUpdate;
                if (objectStatUpdate.getStat().equals(ObjectStat.MaxHullPoints) ||
                        objectStatUpdate.getStat().equals(ObjectStat.MaxPowerPoints))
                {
                    rv = true;
                }
            }
        }

        return rv;
    }
}
