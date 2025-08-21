package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public record MinLevelMaxLevelFactionSwitch(byte minLevel, byte maxLevel) implements IProtocolWrite
{
    public MinLevelMaxLevelFactionSwitch(int minLevel, int maxLevel)
    {
        this((byte) minLevel, (byte) maxLevel);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(minLevel);
        bw.writeByte(maxLevel);
    }
}
