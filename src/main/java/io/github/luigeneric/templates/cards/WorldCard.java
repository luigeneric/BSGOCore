package io.github.luigeneric.templates.cards;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.SpotDesc;

import java.util.Arrays;
import java.util.Optional;

public class WorldCard extends Card
{
    @SerializedName("prefabName")
    private final String prefabName;
    private final byte lodCount;
    private final float radius;
    private final SpotDesc[] spots;
    private final String systemMapTexutres;
    private final byte frameIndex;
    private final byte secondaryFrameIndex;
    @SerializedName("targetable")
    private final boolean targetAble;
    @SerializedName("showBracketWhenInRange")
    private final boolean showBracketsWhenInRange;
    @SerializedName("forceShowOnMap")
    private final boolean forceShowOnMap;

    public WorldCard(long cardGUID, String prefabName, byte lodCount, float radius, SpotDesc[] spots,
                     String systemMapTexture, byte frameIndex, byte secondaryFrameIndex, boolean targetAble,
                     boolean showBracketsWhenInRange, boolean forceShowOnMap)
    {
        super(cardGUID, CardView.World);
        this.prefabName = prefabName;
        this.lodCount = lodCount;
        this.radius = radius;
        this.spots = spots;
        this.systemMapTexutres = systemMapTexture;
        this.frameIndex = frameIndex;
        this.secondaryFrameIndex = secondaryFrameIndex;
        this.targetAble = targetAble;
        this.showBracketsWhenInRange = showBracketsWhenInRange;
        this.forceShowOnMap = forceShowOnMap;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(prefabName);
        bw.writeByte(lodCount);
        bw.writeSingle(radius);
        bw.writeDescArray(spots);
        bw.writeString(systemMapTexutres);
        bw.writeByte(frameIndex);
        bw.writeByte(secondaryFrameIndex);
        bw.writeBoolean(targetAble);
        bw.writeBoolean(showBracketsWhenInRange);
        bw.writeBoolean(forceShowOnMap);
    }

    @Override
    public String toString()
    {
        return "WorldCard{" +
                "prefabName='" + prefabName + '\'' +
                ", lodCount=" + lodCount +
                ", radius=" + radius +
                ", spots=" + Arrays.toString(spots) +
                ", systemMapTexutres='" + systemMapTexutres + '\'' +
                ", frameIndex=" + frameIndex +
                ", secondaryFrameIndex=" + secondaryFrameIndex +
                ", targetAble=" + targetAble +
                ", showBracketsWhenInRange=" + showBracketsWhenInRange +
                ", forceShowOnMap=" + forceShowOnMap +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public String getPrefabName()
    {
        return prefabName;
    }

    public byte getLodCount()
    {
        return lodCount;
    }

    public float getRadius()
    {
        return radius;
    }

    public SpotDesc[] getSpots()
    {
        return spots;
    }
    public Optional<SpotDesc> getSpot(final int objectPointServerHash)
    {
        return Arrays.stream(this.spots)
                .filter(spot -> spot.getObjectPointServerHash() == objectPointServerHash)
                .findFirst();
    }

    public String getSystemMapTexutres()
    {
        return systemMapTexutres;
    }

    public byte getFrameIndex()
    {
        return frameIndex;
    }

    public byte getSecondaryFrameIndex()
    {
        return secondaryFrameIndex;
    }

    public boolean isTargetAble()
    {
        return targetAble;
    }

    public boolean isShowBracketsWhenInRange()
    {
        return showBracketsWhenInRange;
    }

    public boolean isForceShowOnMap()
    {
        return forceShowOnMap;
    }
}
