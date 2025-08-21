package io.github.luigeneric.core;


import io.github.luigeneric.templates.loot.LootTemplate;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class LootTemplates
{
    private final Map<Long, LootTemplate> lootTemplateMap;

    public LootTemplates(final Map<Long, LootTemplate> lootTemplateMap)
    {
        this.lootTemplateMap = Collections.unmodifiableMap(lootTemplateMap);
    }

    public Optional<LootTemplate> get(final long id)
    {
        return Optional.ofNullable(this.lootTemplateMap.get(id));
    }
    public LootTemplate getUnsafe(final long id)
    {
        return this.lootTemplateMap.get(id);
    }
}
