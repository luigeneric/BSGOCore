package io.github.luigeneric.core.spaceentities.statsinfo.buffer;


import io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates.ObjectStatUpdate;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates.PropertyUpdate;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.Owner;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.StatsProtocolSubscriber;
import io.github.luigeneric.templates.utils.ObjectStat;

public class SubscribeProtocolBuffer extends BasePropertyBuffer
{

    public SubscribeProtocolBuffer(Owner owner, StatsProtocolSubscriber statsProtocolSubscriber)
    {
        super(owner, statsProtocolSubscriber);
    }

    @Override
    protected boolean isPropertyUpdateAllowed(final PropertyUpdate propertyUpdate)
    {
        final SpaceUpdateType type = propertyUpdate.getSpaceUpdateType();
        boolean returnType = false;
        switch (type)
        {
            case AddBuff,RemoveBuff, HullPoints -> returnType = true;
            case PowerPoints -> returnType = true;
            case TargetID -> returnType = true;
            case ObjectStat ->
            {
                final ObjectStatUpdate statUd = (ObjectStatUpdate) propertyUpdate;
                if (statUd.getStat().equals(ObjectStat.MaxPowerPoints) ||
                statUd.getStat().equals(ObjectStat.MaxHullPoints))
                    returnType = true;
            }
        }

        return returnType;
    }
}
