package io.github.luigeneric.templates.zonestemplates;

import io.github.luigeneric.linearalgebra.base.Vector3;

import java.util.List;

public record ObjectiveTemplate(
        long objectiveId,
        ObjectiveType type,
        Vector3 position,
        double radius,
        String description,
        List<ScriptableObject> triggerScripts,
        List<ObjectiveTemplate> nextObjectives
) {}

record ScriptableObject(ZoneObjectiveTriggerCondition condition){}