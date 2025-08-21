package io.github.luigeneric.core;

import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.counters.Counters;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@ApplicationScoped
public class CounterUpdater
{
    private final UsersContainer usersContainer;
    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;


    @Scheduled(every = "3s")
    public void run()
    {
        final List<User> usrLst = usersContainer.userList(User::isConnected);
        if (usrLst.isEmpty())
            return;

        for (final User user : usrLst)
        {
            final Player player = user.getPlayer();
            final Counters counters = player.getCounterFacade().counters();
            if (counters.requireUpdate())
            {
                user.send(playerProtocolWriteOnly.writeCounters(counters));
            }
        }
    }
}
