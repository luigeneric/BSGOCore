package io.github.luigeneric.templates.cards;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.utils.MapStarDesc;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
public class GalaxyMapCard extends Card
{
    private final Map<Long, MapStarDesc> stars;
    private final int[] tiers;
    private final int baseScalingMultiplier;
    private final Map<Integer, Integer> sectorScalingMultiplier;

    public GalaxyMapCard(long cardGuid, Map<Long, MapStarDesc> stars, int[] tiers, int baseScalingMultiplier, Map<Integer, Integer> sectorScalingMultiplier)
    {
        super(cardGuid, CardView.GalaxyMap);
        this.sectorScalingMultiplier = sectorScalingMultiplier;
        Objects.requireNonNull(stars, "Stars cannot be null!");
        this.stars = stars;
        this.tiers = tiers;
        this.baseScalingMultiplier = baseScalingMultiplier;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        int len = stars.size();
        bw.writeLength(len);
        for (var entry : stars.entrySet())
        {
            bw.writeDesc(entry.getValue());
        }
        int sizeOfTiers = tiers.length;
        bw.writeLength(sizeOfTiers);
        for (int i = 0; i < sizeOfTiers; i++)
        {
            bw.writeUInt16(tiers[i]);
        }
        bw.writeInt32(baseScalingMultiplier);
        int sizeRcpDiff = sectorScalingMultiplier.size();
        bw.writeLength(sizeRcpDiff);

        /*
        for (int i = 0; i < sizeRcpDiff; i++)
        {
            bw.writeInt32(sectorScalingMultiplier[i][0]);
            bw.writeInt32(sectorScalingMultiplier[i][1]);
        }
         */
        for (Map.Entry<Integer, Integer> integerIntegerEntry : sectorScalingMultiplier.entrySet())
        {
            bw.writeUInt32(integerIntegerEntry.getKey());
            bw.writeUInt32(integerIntegerEntry.getValue());
        }

    }

    @Override
    public String toString()
    {
        return "GalaxyMapCard{" +
                "stars=" + stars +
                ", tiers=" + Arrays.toString(tiers) +
                ", baseScalingMultiplier=" + baseScalingMultiplier +
                ", sectorScalingMultiplier=" + sectorScalingMultiplier +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public static int colonialStartSector()
    {
        return 0;
    }
    public static int cylonStartSector()
    {
        return 6;
    }
    public static int getStartSector(final Faction faction)
    {
        return faction == Faction.Colonial ? colonialStartSector() : cylonStartSector();
    }
    public static int[] getBaseSectorIds(final Faction faction)
    {
        return faction == Faction.Colonial ? colonialBaseSectorIds() : cylonBaseSectorIds();
    }
    public static int[] colonialBaseSectorIds()
    {
        return new int[]{0, 49};
    }
    public static int[] cylonBaseSectorIds()
    {
        return new int[]{6, 50};
    }

    public static boolean isBaseSector(final Faction faction, final long id)
    {
        final int[] baseSectorIds = getBaseSectorIds(faction);
        return baseSectorIds[0] == id || baseSectorIds[1] == id;
    }
    public static boolean isStartSector(final Faction faction, final int id)
    {
        return id == getStartSector(faction);
    }

    public MapStarDesc getStarterSectorForFaction(Faction faction)
    {
        final long starterID = getStartSector(faction);
        return this.stars.get(starterID);
    }

    public Optional<MapStarDesc> getStar(final long sectorID)
    {
        return Optional.ofNullable(this.stars.get(sectorID));
    }
}
