package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class DutyCard extends Card
{
    @SerializedName("Level")
    private final byte level;
    @SerializedName("MaxLevel")
    private final byte maxLevel;
    private final long nextDutyCardGuid;
    private final long counterCardGuid;
    @SerializedName("CounterValue")
    private final int counterValue;
    @SerializedName("Experience")
    private final int experience;
    private final long titleCardGuid;


    public DutyCard(long cardGuid, byte level, byte maxLevel, long nextDutyCardGuid, long counterCardGuid,
                    int counterValue, int experience, long titleCardGuid)
    {
        super(cardGuid, CardView.Duty);
        this.level = level;
        this.maxLevel = maxLevel;
        this.nextDutyCardGuid = nextDutyCardGuid;
        this.counterCardGuid = counterCardGuid;
        this.counterValue = counterValue;
        this.experience = experience;
        this.titleCardGuid = titleCardGuid;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(level);
        bw.writeByte(maxLevel);
        bw.writeGUID(nextDutyCardGuid);
        bw.writeGUID(counterCardGuid);
        bw.writeInt32(counterValue);
        bw.writeInt32(experience);
        bw.writeGUID(titleCardGuid);
    }
}
