package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class CameraFxDesc extends Desc
{
    private final boolean forceDisableBloom;

    public CameraFxDesc(boolean forceDisableBloom)
    {
        super("");
        this.forceDisableBloom = forceDisableBloom;
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeBoolean(forceDisableBloom);
    }
}
