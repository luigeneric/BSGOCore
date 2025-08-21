package io.github.luigeneric.core.protocols.dialog;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

/**
 * Äußerung eines NPCs
 */
public class Remark implements IProtocolWrite
{
    private final byte index;
    private final String phraseRaw;
    private final String symbol;

    public Remark(final byte index, final String phraseRaw, final String symbol)
    {
        this.index = index;
        this.phraseRaw = phraseRaw;
        this.symbol = symbol;
    }
    public Remark(final byte index, final String phraseRaw)
    {
        this(index, phraseRaw, "");
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(index);
        bw.writeString(phraseRaw);
        bw.writeString(""); //animation Tag is not used anymore!
        bw.writeUInt32(0); //camPosition is not used anymore
        bw.writeByte((byte) 0); //not used anymore
        bw.writeString(symbol);
    }
}
