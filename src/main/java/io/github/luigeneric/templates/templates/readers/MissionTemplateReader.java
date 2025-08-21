package io.github.luigeneric.templates.templates.readers;

import com.google.gson.Gson;
import io.github.luigeneric.templates.missiontemplates.MissionTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissionTemplateReader extends TemplateReader
{
    public MissionTemplateReader()
    {
        super(Paths.get("ServerConfigurationUtils","global","MissionTemplateConfiguration"));
    }


    public Map<Integer, MissionTemplate> fetchAllMissionTemplates()
    {
        final List<Path> paths = getFilePaths();

        final Map<Integer, MissionTemplate> missionTemplates = new HashMap<>();


        final Gson gson = gsonBuilder.create();

        for (final Path path : paths)
        {
            final String rawText = readRawTxt(path);
            if (rawText == null)
                continue;
            final MissionTemplate[] missionTemplatesArr = gson.fromJson(rawText, MissionTemplate[].class);
            for (MissionTemplate missionTemplate : missionTemplatesArr)
            {
                final MissionTemplate previous = missionTemplates.put(missionTemplate.id(), missionTemplate);
                if (previous != null)
                    throw new IllegalStateException("MissionTemplate reader: mission card " + previous.id() + " is doubled!!");
            }
        }

        return missionTemplates;
    }

    @Override
    protected void registerAllDeserializer()
    {

    }
}
