package io.github.luigeneric.templates.missiontemplates;


import io.github.luigeneric.enums.Faction;

public record MissionTemplate(int id, long missionGuid, Faction faction, MissionSectorDesc missionSectorDesc, MissionCountEntry[] missionCountEntries)
{
}

