package io.github.luigeneric.core;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@ApplicationScoped
public class FactorUpdater
{
    private final UsersContainer usersContainer;
    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;

    public FactorUpdater(final UsersContainer usersContainer)
    {
        this.usersContainer = usersContainer;
        this.playerProtocolWriteOnly = new PlayerProtocolWriteOnly();
    }

    @Scheduled(every = "1m")
    public void run()
    {
        for (final User user : this.usersContainer.userList(User::isConnected))
        {
            final Set<Integer> removedIds = user.getPlayer().getFactors().removeExpiredItems();

            if (!removedIds.isEmpty())
            {
                final BgoProtocolWriter removedIdsBw = playerProtocolWriteOnly.writeRemoveFactorIds(removedIds);
                final boolean sendResult = user.send(removedIdsBw);
            }

            //user.send(playerProtocolWriteOnly.writeFactors(user.getPlayer().getFactors()));
        }
    }
}
