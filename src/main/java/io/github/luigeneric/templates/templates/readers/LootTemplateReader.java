package io.github.luigeneric.templates.templates.readers;

import com.google.gson.Gson;
import io.github.luigeneric.templates.deserializer.LootTemplateDeserializer;
import io.github.luigeneric.templates.deserializer.ShipItemDeserializer;
import io.github.luigeneric.templates.loot.LootTemplate;
import io.github.luigeneric.templates.shipitems.ShipItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LootTemplateReader extends TemplateReader
{
    public LootTemplateReader()
    {
        super(Paths.get("ServerConfigurationUtils","global","LootTemplates"));
    }

    @Produces
    public Map<Long, LootTemplate> fetchAllLootTemplates()
    {
        final Map<Long, LootTemplate> lootTemplates = new HashMap<>();

        final List<Path> paths = this.getFilePaths();
        final Gson gson = gsonBuilder.create();
        for (final Path path : paths)
        {
            final String rawText = readRawTxt(path);
            if (rawText == null) continue;
            final LootTemplate[] lootTemplateArr = gson.fromJson(rawText, LootTemplate[].class);
            for (LootTemplate lootTemplate : lootTemplateArr)
            {
                final LootTemplate previous = lootTemplates.put(lootTemplate.getId(), lootTemplate);
                if (previous != null)
                {
                    throw new IllegalStateException("Double Entry in LootTemplates!!!");
                }
            }

        }

        return lootTemplates;
    }

    @Override
    protected void registerAllDeserializer()
    {
        gsonBuilder.registerTypeAdapter(LootTemplate.class, new LootTemplateDeserializer());
        gsonBuilder.registerTypeAdapter(ShipItem.class, new ShipItemDeserializer());
    }
}
