package io.github.luigeneric.templates.sectortemplates;

import java.util.List;

public record PlanetoidDesc(
        int respawnTime,
        int respawnResourceTime,
        List<ResourceEntry> resourceEntries,
        int minResources,
        int maxResources
)
{
}
