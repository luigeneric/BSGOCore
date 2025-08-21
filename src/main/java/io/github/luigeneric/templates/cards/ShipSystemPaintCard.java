package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class ShipSystemPaintCard extends Card
{
    @SerializedName("model")
    private final String prefabName;
    private final String paintTexture; //can be empty
    private final long shipCardGuid;

    public ShipSystemPaintCard(long cardGuid, String prefabName, String paintTexture, long shipCardGuid)
    {
        super(cardGuid, CardView.ShipPaint);
        this.prefabName = prefabName;
        this.paintTexture = paintTexture;
        this.shipCardGuid = shipCardGuid;
    }
    public ShipSystemPaintCard(long cardGuid, String prefabName, long shipCardGuid)
    {
        this(cardGuid, prefabName, "", shipCardGuid);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(paintTexture);
        bw.writeString(prefabName);
        bw.writeLength(0);
        bw.writeGUID(shipCardGuid);
    }

    @Override
    public String toString()
    {
        return "ShipSystemPaintCard{" +
                "prefabName='" + prefabName + '\'' +
                ", paintTexture='" + paintTexture + '\'' +
                ", shipCardGuid=" + shipCardGuid +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public String getPrefabName()
    {
        return prefabName;
    }

    public String getPaintTexture()
    {
        return paintTexture;
    }

    public long getShipCardGuid()
    {
        return shipCardGuid;
    }

    public boolean isUseDefaultModel()
    {
        return this.prefabName.equals("default");
    }
}
