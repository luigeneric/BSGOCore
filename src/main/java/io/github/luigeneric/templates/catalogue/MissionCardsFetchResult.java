package io.github.luigeneric.templates.catalogue;


import io.github.luigeneric.templates.cards.MissionCard;
import io.github.luigeneric.templates.cards.RewardCard;

public record MissionCardsFetchResult(MissionCard missionCard, RewardCard rewardCard)
{
    public static MissionCardsFetchResult invalid()
    {
        return new MissionCardsFetchResult(null, null);
    }


    public boolean isValid()
    {
        return missionCard != null && rewardCard != null;
    }
}
