package io.github.luigeneric.templates.templates.readers;

import com.google.gson.*;
import io.github.luigeneric.templates.shipconfigs.ShipConfigTemplate;
import io.github.luigeneric.templates.shipconfigs.ShipConfigs;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ShipConfigReader extends TemplateReader
{
    public ShipConfigReader()
    {
        super(Paths.get("ServerConfigurationUtils","global", "ShipConfigTemplates"));
    }

    public void fetchAndSet()
    {
        final List<ShipConfigTemplate> shipConfigTemplates = new ArrayList<>();
        final List<Path> paths = getFilePaths();
        final Gson gson = gsonBuilder.create();
        for (final Path path : paths)
        {
            final String rawText = readRawTxt(path);
            if (rawText == null) continue;
            final ShipConfigTemplate[] shipConfigTemplate = gson.fromJson(rawText, ShipConfigTemplate[].class);
            shipConfigTemplates.addAll(List.of(shipConfigTemplate));
        }
        ShipConfigs.putAll(shipConfigTemplates);
    }

    @Override
    protected void registerAllDeserializer()
    {
        JsonDeserializer<ShipConfigTemplate> jsonDeserializer = new JsonDeserializer<ShipConfigTemplate>()
        {
            @Override
            public ShipConfigTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                final ShipConfigTemplate res = context.deserialize(json, ShipConfigTemplate.class);
                return res;
            }
        };

        //gsonBuilder.registerTypeAdapter(ShipConfigTemplate.class, jsonDeserializer);
    }
}
