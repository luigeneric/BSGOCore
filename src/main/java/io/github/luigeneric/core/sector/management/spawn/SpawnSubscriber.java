package io.github.luigeneric.core.sector.management.spawn;

public interface SpawnSubscriber
{
    void onSpawn(final SpawnAble newSpawnable, final float spawnTime);
}
