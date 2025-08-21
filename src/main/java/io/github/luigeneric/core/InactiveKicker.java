package io.github.luigeneric.core;

import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.database.DbProvider;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Removes users from container if they are offline for more than m minutes
 */
@Slf4j
public class InactiveKicker
{
    private final UsersContainer usersContainer;
    private final DbProvider dbProvider;
    private final long minutesOfflineBeforeKick;

    public InactiveKicker(UsersContainer usersContainer,
                          DbProvider dbProvider,
                          @ConfigProperty(name = "inactive.kicker.minutes.offline.before.kick") long minutesOfflineBeforeKick
    )
    {
        this.usersContainer = usersContainer;
        this.dbProvider = dbProvider;
        this.minutesOfflineBeforeKick = minutesOfflineBeforeKick;
    }


    @Scheduled(every = "{inactive.kicker.minutes.every.delay}")
    public void run()
    {
        final List<User> usersToKick = new ArrayList<>();
        final BgoTimeStamp now = BgoTimeStamp.now();
        for (final User user : this.usersContainer.values())
        {
            final Optional<AbstractConnection> optConnection = user.getConnection();
            if (optConnection.isPresent())
                continue;

            final Optional<BgoTimeStamp> optLastLogout = user.getPlayer().getLastLogout();
            if (optLastLogout.isEmpty())
            {
                log.error("LastLogout was null but should not be null in InactiveKicker");
                continue;
            }
            final BgoTimeStamp lastLogout = optLastLogout.get();
            final Duration duration = lastLogout.getDuration(now);
            final boolean offlineLongerThan15Minutes = duration.toMinutes() > minutesOfflineBeforeKick;
            if (offlineLongerThan15Minutes)
            {
                //Log.info("User was offline for at least " + minutesOfflineBeforeKick + " minutes " + duration.toMinutes());
                usersToKick.add(user);
            }
        }

        for (final User user : usersToKick)
        {
            this.dbProvider.writePlayerToDb(user.getPlayer());
            usersContainer.remove(user);
        }
    }
}
