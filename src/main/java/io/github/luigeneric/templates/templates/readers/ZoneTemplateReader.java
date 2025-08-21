package io.github.luigeneric.templates.templates.readers;

import com.google.gson.Gson;
import io.github.luigeneric.templates.zonestemplates.ZoneTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ZoneTemplateReader extends TemplateReader
{
    public ZoneTemplateReader()
    {
        super(Paths.get("ServerConfigurationUtils","global", "ZoneTemplates"));
    }


    @Override
    protected void registerAllDeserializer()
    {
    }

    @Produces
    public List<ZoneTemplate> fetchZoneTemplate()
    {
        final List<ZoneTemplate> templates = new ArrayList<>();
        final List<Path> paths = this.getFilePaths();
        final Gson gson = gsonBuilder.create();
        for (Path path : paths)
        {
            final String allText = readRawTxt(path);
            if (allText == null) continue;
            final ZoneTemplate template = gson.fromJson(allText, ZoneTemplate.class);
            templates.add(template);
        }
        return templates;
    }
}

