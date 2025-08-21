package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class MissionCard extends Card
{
    @SerializedName("Level")
    private final int level;
    /**
     * Not used by the client
     */
    @SerializedName("LevelRequirement")
    private final int levelRequirement;
    /**
     * Not used by the client
     */
    @SerializedName("LevelUpperLimit")
    private final int levelUpperLimit;
    private final long rewardCardGuid;
    /**
     * Not used anymore! Default 0
     */
    private final long receiverGuiCardGuid;
    /**
     * Client uses this inside locale_dialogs.xml.
     * If not undefined, uses dock, dradis_contact, friend_invite, ...
     */
    @SerializedName("Action")
    private final String action;

    public MissionCard(long cardGuid, int level, int levelRequirement, int levelUpperLimit, long rewardCardGuid,
                       long receiverGuiCardGuid, String action)
    {
        super(cardGuid, CardView.Mission);

        if (isNotInsideLevelRange(level)) throw new IllegalArgumentException("level is out of level range");
        if (isNotInsideLevelRange(levelRequirement)) throw new IllegalArgumentException("levelRequirement is out of level range");
        if (isNotInsideLevelRange(levelUpperLimit)) throw new IllegalArgumentException("levelUpperLimit is out of level range");

        this.level = level;
        this.levelRequirement = levelRequirement;
        this.levelUpperLimit = levelUpperLimit;
        this.rewardCardGuid = rewardCardGuid;
        this.receiverGuiCardGuid = receiverGuiCardGuid;
        this.action = action;
    }

    private boolean isNotInsideLevelRange(final int value)
    {
        return (0 > value) || (value > 255);
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte((byte) level);
        bw.writeByte((byte) levelRequirement);
        bw.writeByte((byte) levelUpperLimit);
        bw.writeGUID(rewardCardGuid);
        bw.writeGUID(receiverGuiCardGuid);
        bw.writeString(action);
    }

    private boolean checkLevelRequirements(final short level)
    {
        return levelRequirement == 0 || level >= this.levelRequirement;
    }
    private boolean checkUpperLimit(final short level)
    {
        return levelUpperLimit == 0 || level <= levelUpperLimit;
    }

    public boolean checkMinMaxLevel(final short currentLevel)
    {
        return checkLevelRequirements(currentLevel) && checkUpperLimit(currentLevel);
    }

    public long getRewardCardGuid()
    {
        return rewardCardGuid;
    }
}
