package io.github.luigeneric.core.community.guild;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GuildInfoMessage implements IProtocolWrite
{
    private final long guildID;
    private final String guildName;
    private final List<GuildRankDefinition> rankDefinitions;
    private final List<GuildMemberInfo> guildMemberInfos;

    public GuildInfoMessage(final Guild guild)
    {
        this(guild.getId(), guild.getName(), guild.getGuildRankDefinitions(), guild.getGuildMemberInfos());
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(guildID);
        bw.writeString(guildName);
        bw.writeDescCollection(rankDefinitions);
        bw.writeDescCollection(guildMemberInfos);
    }
}
