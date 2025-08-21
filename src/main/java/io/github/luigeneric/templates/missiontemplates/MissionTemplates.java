package io.github.luigeneric.templates.missiontemplates;



import io.github.luigeneric.enums.Faction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MissionTemplates
{
    private static final Map<Integer, MissionTemplate> missionTemplateList = new HashMap<>();
    private static final Map<Integer, MissionTemplate> colonialMissionTemplates = new HashMap<>();
    private static final Map<Integer, MissionTemplate> cylonMissionTemplates = new HashMap<>();

    private MissionTemplates(){}

    public static void injectMissions(final Map<Integer, MissionTemplate> missionTemplates)
    {
        if (!missionTemplateList.isEmpty())
            throw new IllegalStateException("MissionTemplates can only be initialized one time!");

        missionTemplateList.putAll(missionTemplates);

        addForMissionTemplate(missionTemplates, colonialMissionTemplates, Faction.Colonial);
        addForMissionTemplate(missionTemplates, cylonMissionTemplates, Faction.Cylon);
    }

    private static void addForMissionTemplate(final Map<Integer, MissionTemplate> allSet,
                                              final Map<Integer, MissionTemplate> toFill,
                                              final Faction faction
    )
    {
        allSet.values().stream()
                .filter(missionTemplate -> Faction.invert(faction) != missionTemplate.faction())
                        .forEach(missionTemplate -> toFill.put(missionTemplate.id(), missionTemplate));
    }

    private static Map<Integer, MissionTemplate> getCylonMissionTemplates()
    {
        return Collections.unmodifiableMap(cylonMissionTemplates);
    }
    private static Map<Integer, MissionTemplate> getColonialMissionTemplates()
    {
        return Collections.unmodifiableMap(colonialMissionTemplates);
    }

    public static Map<Integer, MissionTemplate> getMissionTemplates(final Faction faction)
    {
        return faction == Faction.Colonial ? getColonialMissionTemplates() : getCylonMissionTemplates();
    }
}
