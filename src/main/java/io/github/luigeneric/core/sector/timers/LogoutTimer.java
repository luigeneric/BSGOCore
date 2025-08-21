package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.AbstractConnection;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.enums.RemovingCause;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class LogoutTimer extends DelayedTimer
{
    private final SectorUsers users;
    private final ISpaceObjectRemover remover;
    public LogoutTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks,
                       final SectorUsers users, final ISpaceObjectRemover remover)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.users = users;
        this.remover = remover;
    }

    @Override
    protected void delayedUpdate()
    {
        if (this.users.isEmpty())
            return;

        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

        for (final User user : users.getUsersCollection())
        {
            final Optional<AbstractConnection> optConnection = user.getConnection();
            if (optConnection.isPresent())
                continue;

            final Optional<BgoTimeStamp> optLastLogout = user.getPlayer().getLastLogout();
            if (optLastLogout.isEmpty())
            {
                log.error(user.getUserLog() + "disconnected but no logout date set");
                continue;
            }

            final BgoTimeStamp lastLogout = optLastLogout.get();
            final boolean isTwoMinutesBeforeNow = lastLogout.isMinutesBeforeDate(1, now);
            final PlayerShip playerShip = users.getPlayerShipUnsafe(user.getPlayer().getUserID());
            if (isTwoMinutesBeforeNow)
            {
                playerShip.getMovementController().getMovementOptions().setSpeedAndThrottle(0);
                playerShip.getMovementController().getMovementOptions().setGear(Gear.Regular);
            }
            final boolean isThreeMinutesBeforeNow = lastLogout.isMinutesBeforeDate(2, now);
            if (isThreeMinutesBeforeNow)
            {
                remover.notifyRemovingCauseAdded(playerShip, RemovingCause.Disconnection);
            }
        }
    }
}
