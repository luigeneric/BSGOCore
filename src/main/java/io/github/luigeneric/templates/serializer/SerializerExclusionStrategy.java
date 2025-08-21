package io.github.luigeneric.templates.serializer;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.Expose;

public class SerializerExclusionStrategy implements ExclusionStrategy
{
    @Override
    public boolean shouldSkipField(FieldAttributes f)
    {
        final Expose anno = f.getAnnotation(Expose.class);
        if (anno == null)
            return false;
        return !anno.serialize();
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz)
    {
        return false;
    }
}
