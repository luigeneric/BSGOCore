package io.github.luigeneric.templates.loot;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.shipitems.ShipItem;

public record LootEntryInfo(float chance, short[] levelIntervall, ShipItem shipItem, float variationPercentage, Faction faction)
{
    public LootEntryInfo(double chance, short[] levelIntervall, ShipItem shipItem, double variationPercentage)
    {
        this((float) chance, levelIntervall, shipItem, (float) variationPercentage, Faction.Neutral);
    }

    public boolean isInLevelIntervall(final short level)
    {
        return this.levelIntervall[0] <= level && level <= this.levelIntervall[1];
    }

    public boolean allowedToReceiveFaction(final Faction faction)
    {
        final Faction currentFaction = faction();
        if (currentFaction == Faction.Neutral)
            return true;

        return currentFaction.equals(faction);
    }

    @Override
    public Faction faction()
    {
        if (this.faction == null)
            return Faction.Neutral;
        return this.faction;
    }
}

