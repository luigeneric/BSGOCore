package io.github.luigeneric.core;

import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.quarkus.virtual.threads.VirtualThreads;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class MissionUpdater
{
    private static final long WAIT_DURATION_BEFORE_SEND_MS = Duration.ofSeconds(1).toMillis();
    private static final Object UPDATE_IN_PROGRESS = new Object();

    private final UsersContainer usersContainer;
    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;
    private final ScheduledExecutorService executorService;

    private final Map<Long, Object> usersUpdateStarted;

    public MissionUpdater(
            UsersContainer usersContainer,
            PlayerProtocolWriteOnly playerProtocolWriteOnly,
            ScheduledExecutorService executorService
    )
    {
        this.usersContainer = usersContainer;
        this.playerProtocolWriteOnly = playerProtocolWriteOnly;
        this.usersUpdateStarted = new ConcurrentHashMap<>();
        this.executorService = executorService;
        log.info("MissionUpdater initialization finished");
    }

    public void updateRequired(final long userId)
    {
        if (usersUpdateStarted.putIfAbsent(userId, UPDATE_IN_PROGRESS) != null)
        {
            return;
        }

        usersContainer.get(userId).ifPresentOrElse(user ->
                {
                    final MissionBook missionBook = user.getPlayer().getCounterFacade().missionBook();
                    executorService.schedule(() ->
                    {
                        try
                        {
                            user.send(playerProtocolWriteOnly.writeMissions(missionBook));
                        } catch (Exception ex)
                        {
                            log.error("Error in MissionUpdater", ex);
                        } finally
                        {
                            usersUpdateStarted.remove(userId);
                        }
                    }, WAIT_DURATION_BEFORE_SEND_MS, TimeUnit.MILLISECONDS);
                },
                () -> usersUpdateStarted.remove(userId)
        );
    }
}
