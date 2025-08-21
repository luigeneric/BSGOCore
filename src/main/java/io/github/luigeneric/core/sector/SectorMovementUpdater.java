package io.github.luigeneric.core.sector;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.SectorSender;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.lootsystem.claims.LootClaimHolder;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SectorMovementUpdater implements SectorJob
{
    private final Tick tick;
    private final SectorSpaceObjects spaceObjects;
    private final SectorSender sectorSender;
    private final ISpaceObjectRemover remover;
    private final LootClaimHolder lootClaimHolder;

    public SectorMovementUpdater(final Tick tick, final SectorSpaceObjects spaceObjects,
                                 final SectorSender sectorSender, final ISpaceObjectRemover remover,
                                 final LootClaimHolder lootClaimHolder
    )
    {
        this.tick = tick;
        this.spaceObjects = spaceObjects;
        this.sectorSender = sectorSender;
        this.remover = remover;
        this.lootClaimHolder = lootClaimHolder;
    }

    @Override
    public void run()
    {
        final Tick copyTick = this.tick.copy();

        //updateOctree();
        final Collection<SpaceObject> spaceObjectsExceptPlayers = this.spaceObjects.getSpaceObjectsNotOfEntityType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Planetoid,
                SpaceEntityType.Planet,
                SpaceEntityType.Debris
        );
        updateForGroup(spaceObjectsExceptPlayers, tick.getDeltaTime(), copyTick);
    }
    
    public void updateForGroup(final Collection<SpaceObject> spaceObjects, final float dt,
                               final Tick copyTick)
    {
        final List<BgoProtocolWriter> movementBws = new ArrayList<>();
        for (final SpaceObject spaceObject : spaceObjects)
        {
            final MovementController movementController = spaceObject.getMovementController();

            movementController.movementUpdateInProgress();
            movementController.move(copyTick, dt);

            if (movementController.isNewManeuver())
            {
                movementController.setLastMovementUpdateTick(copyTick);
                if (sectorSender.getUsers().isEmpty())
                {
                    movementController.movementUpdateFinished();
                    continue;
                }
                final GameProtocolWriteOnly gameProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Game);

                BgoProtocolWriter movementProtocolBuffer;
                if (Objects.requireNonNull(movementController.getCurrentManeuver().getManeuverType()) == ManeuverType.Rest
                        && spaceObject.getMovementController().getFrameTick() != null)
                {
                    movementProtocolBuffer = gameProtocolWriteOnly.writeMove(spaceObject);
                } else
                {
                    try
                    {
                        movementProtocolBuffer = gameProtocolWriteOnly.writeSyncMove(spaceObject);
                    }
                    catch (IllegalArgumentException illegalArgumentException)
                    {
                        movementProtocolBuffer = null;
                        log.error("in writeSyncMove", illegalArgumentException);
                    }

                    //movementProtocolBuffer = gameProtocolWriteOnly.sendMove(currentSpaceObject.getObjectID(), movementController.getManeuver());
                }
                movementController.setIsNewManeuver(false);
                //bws.add(movementProtocolBuffer);
                if (movementProtocolBuffer != null)
                {
                    movementBws.add(movementProtocolBuffer);
                }
            }
            movementController.movementUpdateFinished();
            outOfSectorHandling(spaceObject, movementController);
        }
        this.sectorSender.sendToAllClients(movementBws);
    }

    private void outOfSectorHandling(final SpaceObject spaceObject, final MovementController movementController)
    {
        if (spaceObject.getSpaceEntityType().isOfType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Planet,
                SpaceEntityType.Planetoid,
                SpaceEntityType.Debris)
        )
        {
            return;
        }
        final boolean isOutOfSector = isOutOfSector(movementController.getPosition());
        if (isOutOfSector)
        {
            if (spaceObject.getSpaceEntityType() == SpaceEntityType.Comet)
            {
                lootClaimHolder.removeClaim(spaceObject);
            }
            this.remover.notifyRemovingCauseAdded(spaceObject, RemovingCause.Death);
        }
    }
    private boolean isOutOfSector(final Vector3 position)
    {
        for (final float v : position.toArray())
        {
            if (Mathf.abs(v) > 50_000f)
            {
                return true;
            }
        }
        return false;
    }
}
