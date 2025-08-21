package io.github.luigeneric.core.spaceentities.bindings;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolRead;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.io.IOException;

public class StickerBinding implements IProtocolWrite, IProtocolRead
{
    @SerializedName("ObjectPointHash")
    private int objectPointHash;
    @SerializedName("StickerID")
    private int stickerId;


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt16(objectPointHash);
        bw.writeUInt16(stickerId);
    }


    @Override
    public void read(final BgoProtocolReader br) throws IOException
    {
        this.objectPointHash = br.readUint16();
        this.stickerId = br.readUint16();
    }

    public int getObjectPointHash()
    {
        return objectPointHash;
    }

    public int getStickerId()
    {
        return stickerId;
    }
}