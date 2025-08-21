package io.github.luigeneric.templates.templates.readers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import io.github.luigeneric.templates.augments.AugmentFactorTemplate;
import io.github.luigeneric.templates.augments.AugmentLootItemTemplate;
import io.github.luigeneric.templates.augments.AugmentTeleportTemplate;
import io.github.luigeneric.templates.augments.AugmentTemplate;
import io.github.luigeneric.templates.deserializer.ShipItemDeserializer;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.utils.AugmentActionType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class AugmentTemplateReader extends TemplateReader
{
    public AugmentTemplateReader()
    {
        super(Paths.get("ServerConfigurationUtils","global", "AugmentTemplates"));
    }

    public Map<Long, AugmentTemplate> getAllAugmentTemplates()
    {
        final Map<Long, AugmentTemplate> augmentTemplateMap = new HashMap<>();
        final List<JsonArray> jsonArrays = new ArrayList<>();
        final Gson gson = gsonBuilder.create();
        for (final Path filePath : this.getFilePaths())
        {
            final String allText = readRawTxt(filePath);
            if (allText == null)
                continue;
            final JsonArray jsonArr = gson.fromJson(allText, JsonArray.class);
            jsonArrays.add(jsonArr);
        }
        final JsonArray merged = new JsonArray();
        for (final JsonArray jsonArray : jsonArrays)
        {
            merged.addAll(jsonArray);
        }
        final AugmentTemplate[] templates = gson.fromJson(merged, AugmentTemplate[].class);
        for (final AugmentTemplate template : templates)
        {
            augmentTemplateMap.put(template.getAssociatedItemGUID(), template);
        }
        return Collections.unmodifiableMap(augmentTemplateMap);
    }

    @Override
    protected void registerAllDeserializer()
    {
        final JsonDeserializer<AugmentTemplate> augmentTemplateJsonDeserializer = (json, typeOfT, context) ->
        {
            final var jsonObject = json.getAsJsonObject();
            final String rawAugmentActionType = jsonObject.get("augmentActionType").getAsString();
            final AugmentActionType augmentActionType = AugmentActionType.valueOf(rawAugmentActionType);
            switch (augmentActionType)
            {
                case LootItem ->
                {
                    return context.deserialize(json, AugmentLootItemTemplate.class);
                }
                case Teleport ->
                {
                    return context.deserialize(json, AugmentTeleportTemplate.class);
                }
                case None ->
                {
                    return context.deserialize(json, AugmentFactorTemplate.class);
                }
            }
            throw new IllegalStateException("Not implemented!");
        };

        gsonBuilder.registerTypeAdapter(AugmentTemplate.class, augmentTemplateJsonDeserializer);
        gsonBuilder.registerTypeAdapter(ShipItem.class, new ShipItemDeserializer());
    }
}
