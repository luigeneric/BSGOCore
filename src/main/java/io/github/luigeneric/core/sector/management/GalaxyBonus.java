package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.enums.Faction;
import io.quarkus.arc.Lock;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

@Getter
@ApplicationScoped
public class GalaxyBonus
{
    private float coloOpBonus;
    private float cyloOpBonus;

    private float coloMiningBonus;
    private float cyloMiningBonus;

    @Lock
    public void setOpBonus(final Faction faction, final float opBonus)
    {
        if (faction.equals(Faction.Colonial))
        {
            this.coloOpBonus = (float) (opBonus / 100.);
        } else if (faction.equals(Faction.Cylon))
        {
            this.cyloOpBonus = (float) (opBonus / 100.);
        }
    }

    @Lock
    public void setMiningBonus(final Faction faction, final double miningBonus)
    {
        if (faction.equals(Faction.Colonial))
        {
            this.coloMiningBonus = (float) miningBonus;
        } else if (faction.equals(Faction.Cylon))
        {
            this.cyloMiningBonus = (float) miningBonus;
        }
    }
}
