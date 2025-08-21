package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.sectortemplates.PlayerSpawnAreaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpawnAreas
{
    private final Map<Faction, Map<Integer, SpawnArea>> spawnMap;

    public SpawnAreas()
    {
        this.spawnMap = new HashMap<>();
    }

    /**
     * Only used for setup
     * @param spawnAreaTemplate the template to setup this
     */
    public void addSpawn(final PlayerSpawnAreaTemplate spawnAreaTemplate)
    {
        Map<Integer, SpawnArea> spawns = this.spawnMap.get(spawnAreaTemplate.faction());
        if (spawns == null)
        {
            this.spawnMap.put(spawnAreaTemplate.faction(), new HashMap<>());
            spawns = this.spawnMap.get(spawnAreaTemplate.faction());
        }
        spawns.put(spawnAreaTemplate.id(), new SpawnArea(spawnAreaTemplate));
    }

    public Optional<SpawnArea> getSpawnFor(final Faction faction)
    {
        if (!this.spawnMap.containsKey(faction))
        {
            return Optional.empty();
        }
        final var iterator = this.spawnMap.get(faction).values().iterator();
        if (!iterator.hasNext())
            return Optional.empty();
        return Optional.ofNullable(iterator.next());
    }
}
