package io.github.luigeneric.core;

import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class MissionUpdater
{
    private static final long waitDuration = Duration.ofSeconds(3).toMillis();

    private final UsersContainer usersContainer;
    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;

    private final Map<Long, Thread> usersUpdateStarted;

    public MissionUpdater(UsersContainer usersContainer, PlayerProtocolWriteOnly playerProtocolWriteOnly)
    {
        this.usersContainer = usersContainer;
        this.playerProtocolWriteOnly = playerProtocolWriteOnly;
        this.usersUpdateStarted = new ConcurrentHashMap<>();
        log.info("MissionUpdater initialization finished");
    }

    public void updateRequired(final long userId)
    {
        //log.info("updateRequired={}", userId);
        final boolean alreadyPresent = usersUpdateStarted.containsKey(userId);
        //if present
        if (alreadyPresent)
        {
            //log.info("already present");
            return;
        }
        //log.info("not present");

        final Optional<User> optUser = usersContainer.get(userId);
        if (optUser.isEmpty())
        {
            //log.info("remove user because hes empty");
            return;
        }
        //log.info("initialized update");
        final User user = optUser.get();
        final MissionBook missionBook = user.getPlayer().getCounterFacade().missionBook();

        final Thread virualThread = Thread.ofVirtual().start(() ->
        {
            try
            {
                //log.info("VThread set to sleep");
                Thread.sleep(waitDuration);
                //log.info("sleep finished");
                user.send(playerProtocolWriteOnly.writeMissions(missionBook));
                //log.info("write missions finished");
                usersUpdateStarted.remove(userId);
                //log.info("remove");
            } catch (InterruptedException e)
            {
                log.error(e.getMessage());
            }
        });
        usersUpdateStarted.put(userId, virualThread);
    }
}
