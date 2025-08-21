package io.github.luigeneric.core.sector.management.spawn;

import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;

import java.util.concurrent.TimeUnit;

public abstract class SpawnAble
{
    protected final SpawnController spawnController;
    protected final SpawnSubscriber spawnSubscriber;
    protected final Catalogue catalogue;
    protected SpawnAble next;

    protected SpawnAble(SpawnController spawnController, SpawnSubscriber spawnSubscriber)
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.spawnController = spawnController;
        this.spawnSubscriber = spawnSubscriber;
    }

    public abstract void spawn();
    public abstract SpawnAble getNext();
    public boolean hasNext()
    {
        return this.next != null;
    }

    private boolean isServerJustStarted()
    {
        return this.spawnController.getTick().getTimePassedSinceStart(TimeUnit.MINUTES) < 1;
    }
    protected int adjustSpawnTimeIfIsInitialSpawn(final int spawnTime)
    {
        return isServerJustStarted() ? 10 : spawnTime;
    }
}

