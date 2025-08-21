package io.github.luigeneric.core.sector.management.notifications;


import io.github.luigeneric.core.protocols.notification.MiningShipAttackedType;
import io.github.luigeneric.core.protocols.notification.OutpostAttackedType;
import io.github.luigeneric.core.spaceentities.SpaceObject;

public record NotificationKey(SpaceObject spaceObject, OutpostAttackedType outpostAttackedType,
                              MiningShipAttackedType miningShipAttackedType
)
{
    public NotificationKey(SpaceObject spaceObject, OutpostAttackedType outpostAttackedType)
    {
        this(spaceObject, outpostAttackedType, null);
    }
    public NotificationKey(SpaceObject spaceObject, MiningShipAttackedType miningShipAttackedType)
    {
        this(spaceObject, null, miningShipAttackedType);
    }
}
