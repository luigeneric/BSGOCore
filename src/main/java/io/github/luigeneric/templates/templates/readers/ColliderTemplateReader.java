package io.github.luigeneric.templates.templates.readers;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import io.github.luigeneric.templates.colliderstemplates.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ColliderTemplateReader extends TemplateReader
{
    public ColliderTemplateReader()
    {
        super(Paths.get("ServerConfigurationUtils","global","ColliderTemplates"));
    }

    public List<ColliderTemplate> fetchColliderTemplates()
    {
        final List<ColliderTemplate> colliderTemplatesLst = new ArrayList<>();
        final List<Path> paths = this.getFilePaths();
        final Gson gson = gsonBuilder.create();
        for (final Path path : paths)
        {
            final String allText = readRawTxt(path);
            if (allText == null) continue;
            final ColliderTemplate colliderTemplate = gson.fromJson(allText, ColliderTemplate.class);
            colliderTemplatesLst.add(colliderTemplate);
        }
        return colliderTemplatesLst;
    }

    @Override
    protected void registerAllDeserializer()
    {
        JsonDeserializer<ColliderTemplate> jsonDeserializer = (json, typeOfT, context) ->
        {
            JsonObject jsonObject = json.getAsJsonObject();
            final String rawColliderType = jsonObject.get("type").getAsString();
            final ColliderType colliderType = ColliderType.valueOf(rawColliderType);
            ColliderTemplate colliderTemplate;
            switch (colliderType)
            {
                case AABB ->
                {
                    colliderTemplate = context.deserialize(json, AABBColliderTemplate.class);
                }
                case Mesh ->
                {
                    colliderTemplate = context.deserialize(json, MeshColliderTemplate.class);
                }
                case Sphere ->
                {
                    colliderTemplate = context.deserialize(json, SphereColliderTemplate.class);
                }
                case Capsule ->
                {
                    colliderTemplate = context.deserialize(json, CapsuleColliderTemplate.class);
                }
                default -> {
                    return null;
                }
            }
            return colliderTemplate;
        };
        this.gsonBuilder.registerTypeAdapter(ColliderTemplate.class, jsonDeserializer);
    }
}
