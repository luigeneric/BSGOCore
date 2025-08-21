package io.github.luigeneric.templates.templates.readers;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SectorTemplateReader extends TemplateReader
{
    public SectorTemplateReader()
    {
        super(Paths.get("ServerConfigurationUtils","global", "SectorTemplates"));
    }


    @Override
    protected void registerAllDeserializer()
    {
        JsonDeserializer<SpaceObjectTemplate> jsonDeserializer = (json, typeOfT, context) ->
        {
            JsonObject jsonObject = json.getAsJsonObject();
            final String rawSpaceEntityType = jsonObject.get("spaceEntityType").getAsString();
            final SpaceEntityType entityType = SpaceEntityType.valueOf(rawSpaceEntityType);

            SpaceObjectTemplate spaceObjectTemplate;
            switch (entityType)
            {
                case Cruiser ->
                {
                    spaceObjectTemplate = context.deserialize(json, CruiserTemplate.class);
                }
                case Asteroid ->
                {
                    spaceObjectTemplate = context.deserialize(json, AsteroidTemplate.class);
                }
                case Planetoid ->
                {
                    spaceObjectTemplate = context.deserialize(json, PlanetoidTemplate.class);
                }
                case Planet ->
                {
                    spaceObjectTemplate = context.deserialize(json, PlanetTemplate.class);
                }
                case WeaponPlatform ->
                {
                    spaceObjectTemplate = context.deserialize(json, WeaponPlatformTemplate.class);
                }
                case Outpost ->
                {
                    spaceObjectTemplate = context.deserialize(json, OutpostTemplate.class);
                }
                case Debris ->
                {
                    spaceObjectTemplate = context.deserialize(json, DebrisTemplate.class);
                }
                default ->
                {
                    spaceObjectTemplate = null;
                }
            }
            return spaceObjectTemplate;
        };

        gsonBuilder.registerTypeAdapter(SpaceObjectTemplate.class, jsonDeserializer);
    }

    @Produces
    public List<SectorDesc> fetchSectorTemplates()
    {
        final List<SectorDesc> templates = new ArrayList<>();
        final List<Path> paths = this.getFilePaths();
        final Gson gson = gsonBuilder.create();
        for (Path path : paths)
        {
            final String allText = readRawTxt(path);
            if (allText == null) continue;
            final SectorDesc template = gson.fromJson(allText, SectorDesc.class);
            templates.add(template);
        }
        for (final SectorDesc template : templates)
        {
            for (SpaceObjectTemplate spaceObjectTemplate : template.getSpaceObjectTemplates())
            {
                if (spaceObjectTemplate instanceof AsteroidTemplate asteroidTemplate)
                {
                    asteroidTemplate.setRespawnTime(template.getAsteroidDesc().respawnTime());
                }
                if (spaceObjectTemplate instanceof PlanetoidTemplate planetoidTemplate)
                {
                    planetoidTemplate.setRespawnTime(template.getPlanetoidDesc().respawnTime());
                }
            }
        }
        return templates;
    }
}
