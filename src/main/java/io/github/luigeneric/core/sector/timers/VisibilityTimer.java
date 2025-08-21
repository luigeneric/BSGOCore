package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SpaceObjectRemover;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.bindings.PlayerVisibility;
import io.github.luigeneric.enums.BgoAdminRoles;
import io.github.luigeneric.enums.ChangeVisibilityReason;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class VisibilityTimer extends UpdateTimer
{
    private final SectorContext ctx;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;
    private final SpaceObjectRemover remover;

    public VisibilityTimer(final SectorContext ctx, final SpaceObjectRemover remover
    )
    {
        super(ctx.spaceObjects());
        this.ctx = ctx;
        this.gameProtocolWriteOnly = new GameProtocolWriteOnly();
        this.remover = remover;
    }

    @Override
    public void update(float dt)
    {
        final List<BgoProtocolWriter> bws = new ArrayList<>();


        for (final User user : ctx.users().getUsersCollection())
        {
            final PlayerShip ship = ctx.users().getPlayerShipUnsafe(user.getPlayer().getUserID());
            if (ship == null)
                continue;

            final PlayerVisibility visibility = ship.getPlayerVisibility();

            final GameProtocol gameProtocol = user.getProtocol(ProtocolID.Game);
            final AtomicBoolean completeJumpFlag = gameProtocol.getCompleteJumpFlag();


            if (completeJumpFlag.compareAndSet(true, false))
            {
                final boolean processCorrect = visibility.startGhostJump();
            }

            if (visibility.requiredJumpIn(ctx.tick()))
            {
                visibility.finishGhostJumpInIfNotFinished();
            }

            if (visibility.checkVisibilityRequiresUpdate())
            {
                final BgoProtocolWriter bw = gameProtocolWriteOnly
                        .writeChangeVisibility(ship.getObjectID(), visibility);
                if (visibility.isVisible())
                {
                    bws.add(bw);
                    //get the current system
                    final Location location = user.getPlayer().getLocation();
                    final long currentSectorId = location.getSectorID();
                    try
                    {
                        final boolean isEnemyBaseSector = GalaxyMapCard.isBaseSector(user.getPlayer().getFaction().enemyFaction(), currentSectorId);
                        //is enemy base and not dev or cm
                        if (
                                isEnemyBaseSector
                                &&
                                !(user.getPlayer().getBgoAdminRoles().hasOneRole(BgoAdminRoles.Developer, BgoAdminRoles.CommunityManager))
                        )
                        {
                            log.error("Criticial warning, user is in enemy base sector={}, currentFaction={} sectorID={}",
                                    user.getUserLog(), user.getPlayer().getFaction(), currentSectorId);
                            if (!ship.isRemoved())
                            {
                                remover.notifyRemovingCauseAdded(ship, RemovingCause.Death);
                            }
                        }
                    }
                    catch (IllegalStateException illegalStateException)
                    {
                        log.error("Critical error in visibilityTimer curren user faction is not colonial or cylon");
                    }
                }
                else if (!visibility.isVisible() && visibility.getChangeVisibilityReason() == ChangeVisibilityReason.Anchor)
                {
                    bws.add(bw);
                } else
                {
                    ctx.sender().sendToClient(bw, user);
                }
            }
        }
        ctx.sender().sendToAllClients(bws);
    }
}
