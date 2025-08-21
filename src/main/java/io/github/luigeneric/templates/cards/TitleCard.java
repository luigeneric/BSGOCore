package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.ObjectStats;

public class TitleCard extends Card
{
    @SerializedName("Level")
    private final byte level;
    @SerializedName("StaticBuff")
    private final ObjectStats staticBuff;
    @SerializedName("MultiplyBuff")
    private final ObjectStats multiplyBuff;

    public TitleCard(long cardGuid, byte level, ObjectStats staticBuff, ObjectStats multiplyBuff)
    {
        super(cardGuid, CardView.Title);
        this.level = level;
        this.staticBuff = staticBuff;
        this.multiplyBuff = multiplyBuff;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(level);
        bw.writeString(""); //has to be an empty string...
        bw.writeDesc(staticBuff);
        bw.writeDesc(multiplyBuff);
    }
}
