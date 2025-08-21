package io.github.luigeneric.core;

import io.github.luigeneric.core.player.factors.Factor;
import io.github.luigeneric.core.player.factors.FactorSubscriber;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class FactorRemover implements FactorSubscriber
{
    private final ScheduledExecutorService scheduler;
    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;
    private final UsersContainer usersContainer;

    public FactorRemover(ScheduledExecutorService scheduler, PlayerProtocolWriteOnly playerProtocolWriteOnly, UsersContainer usersContainer)
    {
        this.scheduler = scheduler;
        this.playerProtocolWriteOnly = playerProtocolWriteOnly;
        this.usersContainer = usersContainer;
    }

    @Override
    public void notifyFactorStarted(final long playerID, final Factor factor)
    {
        var optUser = usersContainer.get(playerID);
        if (optUser.isEmpty())
        {
            log.info("FactorRemover but playerID {} is not present", playerID);
            return;
        }
        User user = optUser.get();
        final RemoveFactor removeRunnable = new RemoveFactor(user, factor, playerProtocolWriteOnly);
        LocalDateTime end = factor.getEndTime();
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        final Duration duration = Duration.between(now, end);
        long nanosDuration = duration.toNanos();
        scheduler.schedule(removeRunnable, nanosDuration, TimeUnit.NANOSECONDS);
    }

    @RequiredArgsConstructor
    private static class RemoveFactor implements Runnable
    {
        private final User user;
        private final Factor factor;
        private final PlayerProtocolWriteOnly playerProtocolWriteOnly;

        @Override
        public void run()
        {
            user.getPlayer().getFactors().removeItem(factor.getServerID());
            user.send(playerProtocolWriteOnly.writeRemoveFactorIds(List.of(factor.getServerID())));
        }
    }

    public void shutdown()
    {
        this.scheduler.shutdown();
        boolean closedSuccessfully = false;
        try
        {
            closedSuccessfully = this.scheduler.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e)
        {
            log.warn(e.getMessage());
        }
        if (!closedSuccessfully)
            scheduler.shutdownNow();
    }
}
