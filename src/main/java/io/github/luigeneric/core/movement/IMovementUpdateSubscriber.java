package io.github.luigeneric.core.movement;


import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.templates.utils.ObjectStats;

public interface IMovementUpdateSubscriber
{
    void movementUpdate();
    void setMovementOptionsStats(final SpaceSubscribeInfo spaceSubscribeInfo);
    void setMovementOptionsStats(final ObjectStats stats);
}
