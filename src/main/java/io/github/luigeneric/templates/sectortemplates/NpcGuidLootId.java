package io.github.luigeneric.templates.sectortemplates;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.enums.Faction;

public record NpcGuidLootId(long npcGUID, @SerializedName(value = "lootID", alternate = "lootId") long lootID, Faction faction, int count)
{
    @Override
    public int count()
    {
        if (count == 0)
            return 1;
        else
            return count;
    }
}
