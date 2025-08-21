package io.github.luigeneric.core.community.guild;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.GuildInviteResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GuildInviteResultMessage implements IProtocolWrite
{
    private final long playerID;
    private final GuildInviteResult guildInviteResult;

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt32(playerID);
        bw.writeByte(guildInviteResult.getValue());
    }
}
