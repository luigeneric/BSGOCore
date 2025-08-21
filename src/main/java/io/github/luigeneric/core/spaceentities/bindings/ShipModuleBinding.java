package io.github.luigeneric.core.spaceentities.bindings;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public class ShipModuleBinding implements IProtocolWrite
{
    @SerializedName("ObjectPointHash")
    private final int objectPointHash;
    @SerializedName("Module")
    private final long module;

    public ShipModuleBinding(final int objectPointHash, final long moduleGuid)
    {
        this.objectPointHash = objectPointHash;
        this.module = moduleGuid;
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(objectPointHash);
        bw.writeGUID(module);
    }
}