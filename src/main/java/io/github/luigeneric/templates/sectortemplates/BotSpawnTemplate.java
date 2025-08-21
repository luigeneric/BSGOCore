package io.github.luigeneric.templates.sectortemplates;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.linearalgebra.collidershapes.AABB;

import java.util.List;

/**
 * A template to manage some environment parameters for the bit spawner
 * @param spawnArea
 * @param lifeTimeSeconds
 * @param respawnTimeSeconds the time in seconds it takes until a respawn will be called because of object-jump-out
 * @param respawnTimeDeath the time in seconds it takes until a respawn will be called because of object-death
 * @param faction
 * @param npcSpawnEntries
 */
public record BotSpawnTemplate(AABB spawnArea, long lifeTimeSeconds, long respawnTimeSeconds, long respawnTimeDeath,
                               Faction faction, List<NpcSpawnEntry> npcSpawnEntries)
{
}

