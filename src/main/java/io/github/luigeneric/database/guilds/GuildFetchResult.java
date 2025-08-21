package io.github.luigeneric.database.guilds;


import io.github.luigeneric.core.community.guild.GuildRankDefinition;

import java.util.Set;

public record GuildFetchResult(long id, String name,
                               Set<GuildRankDefinition> guildRankDefinitionSet,
                               Set<MemberInfoRecord> memberInfoRecords
)
{
}