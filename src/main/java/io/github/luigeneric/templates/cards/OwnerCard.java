package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class OwnerCard extends Card
{
    @SerializedName("IsDockable")
    private final boolean isDockable;
    @SerializedName("DockRange")
    private final float dockRange;
    @SerializedName("Level")
    private final byte level;

    public OwnerCard(long cardGuid, boolean isDockable, float dockRange, byte level)
    {
        super(cardGuid, CardView.Owner);
        this.isDockable = isDockable;
        this.dockRange = dockRange;
        this.level = level;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeBoolean(isDockable);
        bw.writeSingle(dockRange);
        bw.writeByte(level);
    }

    public boolean isDockable()
    {
        return isDockable;
    }

    public float getDockRange()
    {
        return dockRange;
    }

    public byte getLevel()
    {
        return level;
    }
}
