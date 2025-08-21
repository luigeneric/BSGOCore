package io.github.luigeneric.templates.sectortemplates;

/**
 * @param extractDelay          In seconds
 * @param npcGuidLootIds        all npc guids with loot associations
 * @param secondsUntilNpcSpawns a delay in seconds until a new assasin spawns
 * @param npcInitialSpawnDelaySeconds the time in seconds until a npc spawns if the miningship was just spawned
 * @param npcLifeTimeSeconds how long the npc has to life until it can jump out
 */
public record MiningShipConfig(int extractPerSecond, int extractDelay, int priceInCubits, NpcGuidLootId[] npcGuidLootIds, int secondsUntilNpcSpawns,
                               int npcInitialSpawnDelaySeconds, long npcLifeTimeSeconds)
{

    @Override
    public int priceInCubits()
    {
        if (priceInCubits == 0)
            return 100;
        return priceInCubits;
    }
}

