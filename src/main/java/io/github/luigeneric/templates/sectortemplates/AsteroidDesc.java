package io.github.luigeneric.templates.sectortemplates;

import java.util.List;

public record AsteroidDesc(
        int respawnTime,
        int respawnResourceTime,
        int[] hpIntervall,
        MaxResourceDesc maxResourceDesc,
        List<ResourceEntry> resourceEntries
)
{
}
