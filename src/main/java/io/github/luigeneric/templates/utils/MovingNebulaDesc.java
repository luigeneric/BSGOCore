package io.github.luigeneric.templates.utils;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.Color;

public class MovingNebulaDesc extends Desc
{
    private final String matSuffix;
    private final String modelName;
    private final Vector3 scale;
    private final Vector2 textureOffset;
    private final Vector2 textureScale;
    private final Color color;


    public MovingNebulaDesc(String matSuffix, String modelName, Quaternion rotation, Vector3 position, Vector3 scale,
                            Vector2 textureOffset, Vector2 textureScale, Color color)
    {
        super("MovingNebula");
        this.matSuffix = matSuffix;
        this.modelName = modelName;
        this.scale = scale;
        this.textureOffset = textureOffset;
        this.textureScale = textureScale;
        this.color = color;
        this.position = position;
        this.rotation = rotation;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeString(matSuffix);
        bw.writeString(modelName);
        bw.writeQuaternion(rotation);
        bw.writeVector3(position);
        bw.writeVector3(scale);
        bw.writeVector2(textureOffset);
        bw.writeVector2(textureScale);
        bw.writeColor(color);
    }
}
