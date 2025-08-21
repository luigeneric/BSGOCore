package io.github.luigeneric.templates.zonestemplates;

import java.util.List;
import java.util.Set;

public record ZoneTemplate(
        long zoneGuid,
        long sectorGuid,
        CronSchedule schedule,
        Set<ZoneObjectivePt> zoneObjectivePoints,
        List<ObjectiveTemplate> objectives,
        List<ScriptableObject> scriptableObjects
) {}


