package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.utils.Color;

public class LightDesc extends Desc
{
    private final Color color;
    private final float intensity;
    protected LightDesc(String name, Quaternion rotation, Color color, float intensity)
    {
        super(name);

        this.color = color;
        this.rotation = rotation;
        this.intensity = intensity;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeQuaternion(rotation);
        bw.writeColor(color);
        bw.writeSingle(intensity);
    }
}
