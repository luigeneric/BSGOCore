package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.SkillGroup;

public class SkillCard extends Card
{
    @SerializedName("Level")
    private final short level;
    @SerializedName("MaxLevel")
    private final short maxLevel;
    /**
     * if 0 there is no next skill card and this is max level card
     */
    private final long nextSkillCardGuid;
    @SerializedName("Hash")
    private final long hash;
    @SerializedName("TrainingTime")
    private final float trainingTime;
    @SerializedName("Price")
    private final int price;
    @SerializedName("Group")
    private final SkillGroup skillGroup;
    @SerializedName("StaticBuff")
    private final ObjectStats staticBuff;
    @SerializedName("MultiplyBuff")
    private final ObjectStats multiplyBuff;
    @SerializedName("RequireSkillHash")
    private final long requiredSkillHash;
    @SerializedName("SortWeight")
    private final int sortWeight;

    public SkillCard(long cardGuid, short level, short maxLevel, long nextSkillCardGuid, long hash, float trainingTime, int price,
                     SkillGroup skillGroup, ObjectStats staticBuff, ObjectStats multiplyBuff, long requiredSkillHash, int sortWeight)
    {
        super(cardGuid, CardView.Skill);
        this.level = level;
        this.maxLevel = maxLevel;
        this.nextSkillCardGuid = nextSkillCardGuid;
        this.hash = hash;
        this.trainingTime = trainingTime;
        this.price = price;
        this.skillGroup = skillGroup;
        this.staticBuff = staticBuff;
        this.multiplyBuff = multiplyBuff;
        this.requiredSkillHash = requiredSkillHash;
        this.sortWeight = sortWeight;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte((byte) level);
        bw.writeByte((byte) maxLevel);
        bw.writeUInt32(nextSkillCardGuid);
        bw.writeUInt32(hash);
        bw.writeSingle(trainingTime);
        bw.writeInt32(price);
        bw.writeByte(skillGroup.value);
        bw.writeDesc(staticBuff);
        bw.writeDesc(multiplyBuff);
        bw.writeUInt32(requiredSkillHash);
        bw.writeUInt16(sortWeight);
    }

    public short getLevel()
    {
        return level;
    }

    public short getMaxLevel()
    {
        return maxLevel;
    }

    public long getNextSkillCardGuid()
    {
        return nextSkillCardGuid;
    }

    public long getHash()
    {
        return hash;
    }

    public float getTrainingTime()
    {
        return trainingTime;
    }

    public int getPrice()
    {
        return price;
    }

    public SkillGroup getSkillGroup()
    {
        return skillGroup;
    }

    public ObjectStats getStaticBuff()
    {
        return staticBuff;
    }

    public ObjectStats getMultiplyBuff()
    {
        return multiplyBuff;
    }

    public long getRequiredSkillHash()
    {
        return requiredSkillHash;
    }

    public int getSortWeight()
    {
        return sortWeight;
    }

    @Override
    public String toString()
    {
        return "SkillCard{" +
                "level=" + level +
                ", maxLevel=" + maxLevel +
                ", nextSkillCardGuid=" + nextSkillCardGuid +
                ", hash=" + hash +
                ", trainingTime=" + trainingTime +
                ", price=" + price +
                ", skillGroup=" + skillGroup +
                ", staticBuff=" + staticBuff +
                ", multiplyBuff=" + multiplyBuff +
                ", requiredSkillHash=" + requiredSkillHash +
                ", sortWeight=" + sortWeight +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }
}
