package io.github.luigeneric.templates.deserializer;

import com.google.gson.*;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;

import java.lang.reflect.Type;

public class TransformDeserializer implements JsonDeserializer<Transform>
{

    @Override
    public Transform deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException
    {
        final JsonObject obj = json.getAsJsonObject();
        final JsonElement rawPosition = obj.get("position");
        Vector3 position;
        if (rawPosition == null)
        {
            position = Vector3.zero();
        }
        else
        {
            position = context.deserialize(rawPosition, Vector3.class);
        }
        final JsonElement rawRotation = obj.get("rotation");
        final JsonObject rotationObj = rawRotation.getAsJsonObject();
        final boolean isNoYaw = rotationObj.get("yaw").isJsonNull();
        Quaternion rotation;
        if (isNoYaw)
        {
            rotation = context.deserialize(rawRotation, Quaternion.class);
        }
        else
        {
            rotation = ((Euler3)(context.deserialize(rawRotation, Euler3.class))).quaternion();
        }

        return new Transform(position, rotation);
    }
}
