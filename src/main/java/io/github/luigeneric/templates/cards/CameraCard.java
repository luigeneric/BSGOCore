package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class CameraCard extends Card
{
    @SerializedName("DefaultZoom")
    private float defaultZoom;
    @SerializedName("MinZoom")
    private float minZoom;
    @SerializedName("MaxZoom")
    private float maxZoom;
    @SerializedName("SoftTrembleSpeed")
    private float softTrembleSpeed;
    @SerializedName("HardTrembleSpeed")
    private float hardTrembleSpeed;

    public CameraCard(long cardGuid, float defaultZoom, float minZoom, float maxZoom, float softTrembleSpeed, float hardTrembleSpeed)
    {
        super(cardGuid, CardView.Camera);
        this.defaultZoom = defaultZoom;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.softTrembleSpeed = softTrembleSpeed;
        this.hardTrembleSpeed = hardTrembleSpeed;
    }
    public CameraCard(long cardGuid)
    {
        this(cardGuid, 1f, 10f, 20f, 1f, 1f);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeSingle(defaultZoom);
        bw.writeSingle(maxZoom);
        bw.writeSingle(minZoom);
        bw.writeSingle(softTrembleSpeed);
        bw.writeSingle(hardTrembleSpeed);
    }

    @Override
    public String toString()
    {
        return "CameraCard{" +
                "defaultZoom=" + defaultZoom +
                ", minZoom=" + minZoom +
                ", maxZoom=" + maxZoom +
                ", softTrembleSpeed=" + softTrembleSpeed +
                ", hardTrembleSpeed=" + hardTrembleSpeed +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }
}
