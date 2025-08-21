package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.utils.Color;

public class GlobalFogDesc extends Desc
{
    private final boolean enabled;
    private final Color color;
    private final float density;
    private final float startDistance;

    public GlobalFogDesc(boolean enabled, Color color, float density, float startDistance)
    {
        super("");
        this.enabled = enabled;
        this.color = color;
        this.density = density;
        this.startDistance = startDistance;
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeBoolean(enabled);
        bw.writeColor(color);
        bw.writeSingle(density);
        bw.writeSingle(startDistance);
    }
}
