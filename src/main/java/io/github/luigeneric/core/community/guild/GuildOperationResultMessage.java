package io.github.luigeneric.core.community.guild;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.GuildOperation;
import io.github.luigeneric.enums.GuildOperationResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GuildOperationResultMessage implements IProtocolWrite
{
    private final GuildOperation guildOperation;
    private final GuildOperationResult guildOperationResult;


    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt32(guildOperation.getValue());
        bw.writeByte(guildOperationResult.getValue());
    }
}
