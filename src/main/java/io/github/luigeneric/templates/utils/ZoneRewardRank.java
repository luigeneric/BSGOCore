package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public class ZoneRewardRank implements IProtocolWrite
{
    private final byte rank;
    private final long guid;

    public ZoneRewardRank(byte rank, long guid)
    {
        this.rank = rank;
        this.guid = guid;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(rank);
        bw.writeUInt32(guid);
    }
}