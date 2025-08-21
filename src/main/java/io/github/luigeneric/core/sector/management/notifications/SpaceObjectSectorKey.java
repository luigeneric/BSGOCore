package io.github.luigeneric.core.sector.management.notifications;


import io.github.luigeneric.core.protocols.notification.MiningShipAttackedType;
import io.github.luigeneric.core.protocols.notification.OutpostAttackedType;

public record SpaceObjectSectorKey(long spaceObjectID,
                                   long sectorGUID,
                                   OutpostAttackedType outpostAttackedType,
                                   MiningShipAttackedType miningShipAttackedType
)
{
    public SpaceObjectSectorKey(long spaceObjectID, long sectorGUID, OutpostAttackedType outpostAttackedType)
    {
        this(spaceObjectID, sectorGUID, outpostAttackedType, null);
    }
    public SpaceObjectSectorKey(long spaceObjectID, long sectorGUID, MiningShipAttackedType miningShipAttackedType)
    {
        this(spaceObjectID, sectorGUID, null, miningShipAttackedType);
    }
}
