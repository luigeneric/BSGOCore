package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.Color;

public class SunDesc extends Desc
{
    private final Color raysColor;
    private final Color streakColor;
    private final Color glowColor;
    private final Color discColor;
    private final boolean occlusionFade;
    private final Vector3 scale;


    public SunDesc(String name, Color raysColor, Color streakColor, Color glowColor, Color discColor,
                      boolean occlusionFade, Vector3 scale, Quaternion rotation, Vector3 position)
    {
        this(name, raysColor, streakColor, glowColor, discColor, occlusionFade, scale);
        this.rotation = rotation;
        this.position = position;
    }

    public SunDesc(String name, Color raysColor, Color streakColor, Color glowColor, Color discColor,
                   boolean occlusionFade, Vector3 scale)
    {
        super(name);
        this.raysColor = raysColor;
        this.streakColor = streakColor;
        this.glowColor = glowColor;
        this.discColor = discColor;
        this.occlusionFade = occlusionFade;
        this.scale = scale;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeColor(raysColor);
        bw.writeColor(streakColor);
        bw.writeColor(glowColor);
        bw.writeColor(discColor);
        bw.writeBoolean(occlusionFade);
        bw.writeQuaternion(rotation);
        bw.writeVector3(position);
        bw.writeVector3(scale);
    }
}
