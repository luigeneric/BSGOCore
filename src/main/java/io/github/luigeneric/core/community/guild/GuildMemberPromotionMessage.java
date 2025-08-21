package io.github.luigeneric.core.community.guild;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.GuildRole;

public class GuildMemberPromotionMessage implements IProtocolWrite
{
    private final long playerID;
    private final GuildRole guildRole;

    public GuildMemberPromotionMessage(final long playerID, final GuildRole guildRole)
    {
        this.playerID = playerID;
        this.guildRole = guildRole;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(playerID);
        bw.writeByte(guildRole.getValue());
    }
}
