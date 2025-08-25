package io.github.luigeneric;

import io.github.luigeneric.core.IServerListener;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.galaxy.EloBalancer;
import io.github.luigeneric.core.protocols.player.CharacterServices;
import io.github.luigeneric.core.protocols.player.MinLevelMaxLevelFactionSwitch;
import io.github.luigeneric.networking.ServerListener;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@RequiredArgsConstructor
public class Configuration
{
    @Inject
    UsersContainer usersContainer;
    @Inject
    GameServerParamsConfig gameServerParamsConfig;


    @Produces
    public CharacterServices characterServices()
    {
        return new CharacterServices(0,
                false,
                gameServerParamsConfig.factionChangeParams().cooldownFactionSwitch(),
                0,
                0,
                0,
                gameServerParamsConfig.factionChangeParams().cubitsPriceFaction(),
                10_000_000, List.of(new MinLevelMaxLevelFactionSwitch(10, 255)));
    }

    @Produces
    public EloBalancer eloBalancer()
    {
        return new EloBalancer(usersContainer, TimeUnit.SECONDS, 10);
    }

    @Produces
    public IServerListener serverListener()
    {
        return new ServerListener(gameServerParamsConfig.port(), gameServerParamsConfig.maxBacklog());
    }
}
