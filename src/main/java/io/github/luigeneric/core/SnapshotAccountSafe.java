package io.github.luigeneric.core;



import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.player.Player;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SnapshotAccountSafe
{
    private final UsersContainer users;
    private final DbProvider dbProvider;
    private static final TimeUnit usedUnit = TimeUnit.MINUTES;
    private static final long delay = 10;
    public SnapshotAccountSafe(final UsersContainer users, final DbProvider dbProvider)
    {
        this.users = users;
        this.dbProvider = dbProvider;
    }


    @Scheduled(every = "10m")
    public void run()
    {
        //final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        final List<Player> usersOnline = users
                .userList(User::isConnected)
                .stream()
                .map(User::getPlayer)
                .toList();

        if (usersOnline.isEmpty())
            return;

        dbProvider.bulkWritePlayerToDb(usersOnline);
    }

    private boolean getRequiresUpdate(final LocalDateTime now, final LocalDateTime last, final User user)
    {
        final Duration duration = Duration.between(last, now);
        final long durationNanos = duration.toNanos();
        final long delayNanos = usedUnit.toNanos(delay);
        final boolean requiresUpdate = durationNanos > delayNanos;
        if (requiresUpdate)
        {
            dbProvider.writePlayerToDb(user.getPlayer());
            user.setLastAccountSafe(now);
        }
        return requiresUpdate;
    }
}
