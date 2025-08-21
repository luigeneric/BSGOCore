package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.*;
import io.github.luigeneric.utils.Color;

import java.util.Arrays;

public class SectorCard extends Card
{
    private final float width; //x
    private final float height; //y
    private final float length; //z
    private final long regulationCardGuid;
    private final Color ambientColor;
    private final Color fogColor;
    private final int fogDensity;
    private final Color dustColor;
    private final int dustDensity;
    private final BackgroundDesc nebulaDesc;
    private final BackgroundDesc starsDesc;
    private final BackgroundDesc starsMultDesc;
    @SerializedName("StarsVarianceDesc")
    private final BackgroundDesc starsVariantDesc;
    private final MovingNebulaDesc[] movingNebulaDescs;
    private final LightDesc[] lightDescs;
    private final SunDesc[] sunDescs;
    private final GlobalFogDesc globalFogDesc;
    private final CameraFxDesc cameraFxDesc;
    private final String[] requiredAssets;
    public SectorCard(long cardGUID, float width, float height, float length, long regulationCardGuid, Color ambientColor,
                      Color fogColor, int fogDensity, Color dustColor, int dustDensity, BackgroundDesc nebulaDesc,
                      BackgroundDesc starsDesc, BackgroundDesc starsMultDesc, BackgroundDesc starsVariantDesc,
                      MovingNebulaDesc[] movingNebulaDescs, LightDesc[] lightDescs, SunDesc[] sunDescs,
                      GlobalFogDesc globalFogDesc, CameraFxDesc cameraFxDesc, String[] requiredAssets)
    {
        super(cardGUID, CardView.Sector);
        this.width = width;
        this.height = height;
        this.length = length;
        this.regulationCardGuid = regulationCardGuid;
        this.ambientColor = ambientColor;
        this.fogColor = fogColor;
        this.fogDensity = fogDensity;
        this.dustColor = dustColor;
        this.dustDensity = dustDensity;
        this.nebulaDesc = nebulaDesc;
        this.starsDesc = starsDesc;
        this.starsMultDesc = starsMultDesc;
        this.starsVariantDesc = starsVariantDesc;
        this.movingNebulaDescs = movingNebulaDescs;
        this.lightDescs = lightDescs;
        this.sunDescs = sunDescs;
        this.globalFogDesc = globalFogDesc;
        this.cameraFxDesc = cameraFxDesc;
        this.requiredAssets = requiredAssets;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeSingle(width);
        bw.writeSingle(height);
        bw.writeSingle(length);
        bw.writeUInt32(regulationCardGuid);
        bw.writeColor(ambientColor);
        bw.writeColor(fogColor);
        bw.writeInt32(fogDensity);
        bw.writeColor(dustColor);
        bw.writeInt32(dustDensity);
        bw.writeDesc(nebulaDesc);
        bw.writeDesc(starsDesc);
        bw.writeDesc(starsMultDesc);
        bw.writeDesc(starsVariantDesc);

        bw.writeDescArray(movingNebulaDescs);
        bw.writeDescArray(lightDescs);
        bw.writeDescArray(sunDescs);

        bw.writeDesc(globalFogDesc);
        bw.writeDesc(cameraFxDesc);
        bw.writeStringArray(requiredAssets);
    }

    @Override
    public String toString()
    {
        return "SectorCard{" +
                "width=" + width +
                ", height=" + height +
                ", length=" + length +
                ", regulationCardGuid=" + regulationCardGuid +
                ", ambientColor=" + ambientColor +
                ", fogColor=" + fogColor +
                ", fogDensity=" + fogDensity +
                ", dustColor=" + dustColor +
                ", dustDensity=" + dustDensity +
                ", nebulaDesc=" + nebulaDesc +
                ", starsDesc=" + starsDesc +
                ", starsMultDesc=" + starsMultDesc +
                ", starsVariantDesc=" + starsVariantDesc +
                ", movingNebulaDescs=" + Arrays.toString(movingNebulaDescs) +
                ", lightDescs=" + Arrays.toString(lightDescs) +
                ", sunDescs=" + Arrays.toString(sunDescs) +
                ", globalFogDesc=" + globalFogDesc +
                ", cameraFxDesc=" + cameraFxDesc +
                ", requiredAssets=" + Arrays.toString(requiredAssets) +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    public float getLength()
    {
        return length;
    }

    public long getRegulationCardGuid()
    {
        return regulationCardGuid;
    }

    public Color getAmbientColor()
    {
        return ambientColor;
    }

    public Color getFogColor()
    {
        return fogColor;
    }

    public int getFogDensity()
    {
        return fogDensity;
    }

    public Color getDustColor()
    {
        return dustColor;
    }

    public int getDustDensity()
    {
        return dustDensity;
    }

    public BackgroundDesc getNebulaDesc()
    {
        return nebulaDesc;
    }

    public BackgroundDesc getStarsDesc()
    {
        return starsDesc;
    }

    public BackgroundDesc getStarsMultDesc()
    {
        return starsMultDesc;
    }

    public BackgroundDesc getStarsVariantDesc()
    {
        return starsVariantDesc;
    }

    public MovingNebulaDesc[] getMovingNebulaDescs()
    {
        return movingNebulaDescs;
    }

    public LightDesc[] getLightDescs()
    {
        return lightDescs;
    }

    public SunDesc[] getSunDescs()
    {
        return sunDescs;
    }

    public GlobalFogDesc getGlobalFogDesc()
    {
        return globalFogDesc;
    }

    public CameraFxDesc getCameraFxDesc()
    {
        return cameraFxDesc;
    }

    public String[] getRequiredAssets()
    {
        return requiredAssets;
    }
}
