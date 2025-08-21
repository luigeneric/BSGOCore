package io.github.luigeneric.templates.utils;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipItemWriterWithoutID;

import java.util.List;

public class ZoneBracketInfo implements IProtocolWrite
{
    @SerializedName("BracketId")
    private final short bracketId;
    @SerializedName("MinLevel")
    private final short minLevel;
    @SerializedName("MaxLevel")
    private final short maxLevel;
    @SerializedName("Rewards")
    private final ZoneRewardRank[] rewards;
    @SerializedName("Admission")
    private final List<ShipItem> shipItems;

    public ZoneBracketInfo(short bracketId, short minLevel, short maxLevel, ZoneRewardRank[] rewards, List<ShipItem> shipItems)
    {
        this.bracketId = bracketId;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.rewards = rewards;
        this.shipItems = shipItems;
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte((byte) bracketId);
        bw.writeByte((byte) minLevel);
        bw.writeByte((byte) maxLevel);
        bw.writeDescArray(rewards);

        ShipItemWriterWithoutID.write(bw, shipItems);
    }
}

