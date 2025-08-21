package io.github.luigeneric.database.guilds;

import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.enums.GuildRole;

public record MemberInfoRecord(GuildRole guildRole, Player player)
{
}
