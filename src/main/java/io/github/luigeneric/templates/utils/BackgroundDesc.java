package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.Color;

public class BackgroundDesc extends Desc
{
    private final String prefabName;
    private Color color;

    public BackgroundDesc(String modelName, Quaternion rotation, Color color, Vector3 position)
    {
        this(modelName, color);
        this.rotation = rotation;
        this.position = position;
    }

    public BackgroundDesc(String modelName, Color color)
    {
        super("Background");
        this.prefabName = modelName;
        this.color = color;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeString(prefabName);
        bw.writeQuaternion(rotation);
        bw.writeColor(color);
        bw.writeVector3(position);
    }

    @Override
    public String toString()
    {
        return "BackgroundDesc{" +
                "prefabName='" + prefabName + '\'' +
                ", color=" + color +
                ", position=" + position +
                ", rotation=" + rotation +
                '}';
    }
}
