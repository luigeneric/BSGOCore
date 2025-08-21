package io.github.luigeneric.templates.utils;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;


public class SpotDesc implements IProtocolWrite
{
    private final int objectPointServerHash;


    private final String objectPointName;


    @SerializedName("type")
    private final SpotType type;


    private final Vector3 localPosition;


    private final Quaternion localRotation;
    private Transform transformCached;

    public SpotDesc(int objectPointServerHash, String objectPointName, SpotType type, Vector3 localPosition, Quaternion localRotation)
    {
        this.objectPointServerHash = objectPointServerHash;
        this.objectPointName = objectPointName;
        this.type = type;
        this.localPosition = localPosition;
        this.localRotation = localRotation;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(objectPointServerHash);
        bw.writeString(objectPointName);
        bw.writeByte(type.getValue());
        bw.writeVector3(localPosition);
        bw.writeQuaternion(localRotation);
    }

    @Override
    public String toString()
    {
        return "SpotDesc{" +
                "objectPointServerHash=" + objectPointServerHash +
                ", objectPointName='" + objectPointName + '\'' +
                ", type=" + type +
                ", localPosition=" + localPosition +
                ", localRotation=" + localRotation +
                '}';
    }

    public int getObjectPointServerHash()
    {
        return objectPointServerHash;
    }

    public String getObjectPointName()
    {
        return objectPointName;
    }

    public SpotType getType()
    {
        return type;
    }

    public Vector3 getLocalPosition()
    {
        return localPosition;
    }

    public Quaternion getLocalRotation()
    {
        return localRotation;
    }

    public Transform getLocalTransform()
    {
        if (transformCached == null)
            transformCached = new Transform(localPosition, localRotation);
        return transformCached;
    }
}
