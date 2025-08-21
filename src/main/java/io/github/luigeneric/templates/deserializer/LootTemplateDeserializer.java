package io.github.luigeneric.templates.deserializer;

import com.google.gson.*;
import io.github.luigeneric.templates.loot.LootDamageRadiusTemplate;
import io.github.luigeneric.templates.loot.LootDamageTemplate;
import io.github.luigeneric.templates.loot.LootTemplate;
import io.github.luigeneric.templates.loot.LootTemplateType;

import java.lang.reflect.Type;

public class LootTemplateDeserializer implements JsonDeserializer<LootTemplate>
{

    @Override
    public LootTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final JsonObject obj = json.getAsJsonObject();
        final String rawType = obj.get("type").getAsString();
        final LootTemplateType lootTemplateType = LootTemplateType.valueOf(rawType);
        switch (lootTemplateType)
        {
            case Damage ->
            {
                return context.deserialize(json, LootDamageTemplate.class);
            }
            case RadiusDamage ->
            {
                return context.deserialize(json, LootDamageRadiusTemplate.class);
            }
            default -> throw new IllegalStateException("LootTemplateType " + lootTemplateType + " not implemented!");
        }
    }
}
