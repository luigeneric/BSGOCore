package io.github.luigeneric.templates.deserializer;

import com.google.gson.*;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ItemType;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.lang.reflect.Type;

public class ShipItemDeserializer implements JsonDeserializer<ShipItem>
{
    @Override
    public ShipItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final JsonObject obj = json.getAsJsonObject();
        final JsonElement first = obj.get("itemType");
        final JsonElement second = obj.get("type");
        final ItemType itemType = ItemType.valueOf(first == null ? second.getAsString() : first.getAsString());


        final long cardGUID = obj.get("cardGuid").getAsLong();

        switch (itemType)
        {
            case Countable ->
            {
                return ItemCountable.fromGUID(cardGUID, obj.get("count").getAsLong());
            }
            case System ->
            {
                return null;
            }
            default -> throw new IllegalStateException("Not implemented");
        }
    }
}
