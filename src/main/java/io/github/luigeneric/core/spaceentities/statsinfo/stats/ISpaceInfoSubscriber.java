package io.github.luigeneric.core.spaceentities.statsinfo.stats;


import io.github.luigeneric.core.sector.ShipModifier;

import java.util.List;
import java.util.Set;

public interface ISpaceInfoSubscriber
{
    void onStatInfoChanged(final SpaceSubscribeInfo spaceSubscribeInfo, final StatInfo statInfo);
    void onSlotUpdate(final SpaceSubscribeInfo spaceSubscribeInfo, final int slotID);
    void onModifierAdd(final SpaceSubscribeInfo spaceSubscribeInfo, final List<ShipModifier> newBuffs);
    void onModifierRemove(final SpaceSubscribeInfo spaceSubscribeInfo, final Set<Long> removedBuffIDs);
}
