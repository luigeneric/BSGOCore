package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSender;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MovementHeartbeat extends DelayedTimer
{
    public static final int HEARTBEAT_INTERVAL_SECONDS = 7;
    private final SectorUsers users;
    private final SectorSender sender;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;
    public MovementHeartbeat(final Tick tick,
                             final SectorSpaceObjects sectorSpaceObjects,
                             final long delayedTicks,
                             final SectorUsers users,
                             final SectorSender sender)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.users = users;
        this.sender = sender;
        gameProtocolWriteOnly = ProtocolRegistryWriteOnly.game();
    }

    @Override
    protected void delayedUpdate()
    {
        if (users.isEmpty())
            return;

        final Tick tickCpy = this.tick.copy();
        beatForDynamicObjects(tickCpy);
    }

    private void beatForDynamicObjects(final Tick tickCpy)
    {
        final List<SpaceObject> heartBeatObjects = sectorSpaceObjects.getSpaceObjectsNotOfEntityType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Planetoid,
                SpaceEntityType.Planet,
                SpaceEntityType.WeaponPlatform,
                SpaceEntityType.MiningShip
        );
        if (heartBeatObjects.isEmpty())
            return;

        final List<BgoProtocolWriter> bws = new ArrayList<>();

        for (final SpaceObject heartBeatObject : heartBeatObjects)
        {
            if (heartBeatObject.isRemoved())
                continue;

            final Tick last = heartBeatObject.getMovementController().getLastMovementUpdateTick();
            //is the last update 5 seconds behind the current tick?
            if (last == null || last.isBehindBy(tick, TimeUnit.SECONDS, HEARTBEAT_INTERVAL_SECONDS))
            {
                if (heartBeatObject.getMovementController().getFrameTick() == null)
                    continue;
                heartBeatObject.getMovementController().setLastMovementUpdateTick(tickCpy);
                try
                {
                    bws.add(gameProtocolWriteOnly.writeSyncMove(heartBeatObject));
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    log.error("in MovementHearbeat ", illegalArgumentException);
                }
            }
        }

        this.sender.sendToAllClients(bws);
    }
}
