package io.github.luigeneric.core;

import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.core.galaxy.Galaxy;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.ScheduledExecutorService;

@Getter
@Accessors(fluent = true)
public final class ProtocolContext
{
    private final AbstractConnection connection;
    private final Catalogue catalogue;
    private final GameServerParamsConfig gameServerParams;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MicrometerRegistry micrometerRegistry;
    private final Galaxy galaxy;
    private final BgoRandom rng;
    @Setter
    private volatile User user;

    public ProtocolContext(AbstractConnection connection, Catalogue catalogue, GameServerParamsConfig gameServerParams, ScheduledExecutorService scheduledExecutorService,
                           MicrometerRegistry micrometerRegistry, BgoRandom rng, Galaxy galaxy,
                           @Nullable User user
    )
    {
        this.connection = connection;
        this.catalogue = catalogue;
        this.gameServerParams = gameServerParams;
        this.scheduledExecutorService = scheduledExecutorService;
        this.micrometerRegistry = micrometerRegistry;
        this.galaxy = galaxy;
        this.rng = rng;
        this.user = user;
    }
}
