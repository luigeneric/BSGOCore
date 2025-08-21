package io.github.luigeneric.templates.cards;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.templates.utils.ObjectStats;

public class NonShipStatsCard extends Card
{
    @SerializedName("Stats")
    private final ObjectStats stats;
    public NonShipStatsCard(final long cardGuid, final ObjectStats stats)
    {
        super(cardGuid, CardView.NonShipStats);
        this.stats = stats;
    }

    public ObjectStats getStats()
    {
        return stats.getCopy();
    }
}
