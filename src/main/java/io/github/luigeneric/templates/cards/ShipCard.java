package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.ShipRoleDeprecated;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.ShipImmutableSlot;
import io.github.luigeneric.templates.utils.ShipRole;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipCard extends Card
{
    @SerializedName("ShipObjectKey")
    private final long shipObjectKey;
    @SerializedName("Level")
    private final byte level;
    @SerializedName("HangarID")
    private final byte hangarId;
    @SerializedName("MaxLevel")
    private final byte maxLevel;
    @SerializedName("LevelRequirement")
    private final short levelRequirement;
    @SerializedName("Durability")
    private final float durability;
    @SerializedName("Tier")
    private final byte tier;
    @SerializedName("ShipRoles")
    private final ShipRole[] shipRoles;
    @SerializedName("ShipRoleDeprecated")
    private final ShipRoleDeprecated shipRoleDeprecated;
    @SerializedName("PaperdollUiLayoutfile")
    private final String paperdollUiLayoutfile;
    @SerializedName("Slots")
    private final ShipSlotCard[] shipSlotCards;
    @SerializedName("CubitOnlyRepair")
    private final boolean cubitOnlyRepair;
    @SerializedName("VariantHangarIDs")
    private final List<Long> variantHangarIDs;
    @SerializedName("ParentHangarID")
    private final int parentHangarId;
    @SerializedName("Stats")
    private final ObjectStats stats;
    @SerializedName("Faction")
    private final Faction faction;
    @SerializedName("ImmutableSlots")
    private final ShipImmutableSlot[] immutableSlots;
    private final long nextShipCardGuid;

    public ShipCard(long cardGUID, long shipObjectKey, byte level, byte hangarId, byte maxLevel, byte levelRequirement,
                    float durability, byte tier, ShipRole[] shipRoles, ShipRoleDeprecated shipRoleDeprecated, String paperdollUiLayoutfile,
                    ShipSlotCard[] shipSlotCards, boolean cubitOnlyRepair, List<Long> variantHangarIDs, int parentHangarId, ObjectStats stats,
                    Faction faction, ShipImmutableSlot[] immutableSlots, long nextShipCardGuid)
    {
        super(cardGUID, CardView.Ship);
        this.shipObjectKey = shipObjectKey;
        this.level = level;
        this.hangarId = hangarId;
        this.maxLevel = maxLevel;
        this.levelRequirement = levelRequirement;
        this.durability = durability;
        this.tier = tier;
        this.shipRoles = shipRoles;
        this.shipRoleDeprecated = shipRoleDeprecated;
        this.paperdollUiLayoutfile = paperdollUiLayoutfile;
        this.shipSlotCards = shipSlotCards;
        this.cubitOnlyRepair = cubitOnlyRepair;
        this.variantHangarIDs = variantHangarIDs;
        this.parentHangarId = parentHangarId;
        this.stats = stats;
        this.faction = faction;
        this.immutableSlots = immutableSlots;
        this.nextShipCardGuid = nextShipCardGuid;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeGUID(shipObjectKey);
        bw.writeByte(level);
        bw.writeByte(maxLevel);
        bw.writeByte((byte) levelRequirement);
        bw.writeByte(hangarId);
        bw.writeGUID(nextShipCardGuid);
        bw.writeSingle(durability);
        bw.writeByte(tier);

        bw.writeLength(shipRoles.length);
        for (ShipRole shipRole : shipRoles)
        {
            bw.writeByte(shipRole.value);
        }
        bw.writeByte(shipRoleDeprecated.getValue());
        bw.writeString(paperdollUiLayoutfile);
        bw.writeDescArray(shipSlotCards);
        bw.writeBoolean(cubitOnlyRepair);
        bw.writeUInt32Collection(variantHangarIDs);
        bw.writeInt32(parentHangarId);
        bw.writeDesc(stats);
        bw.writeByte((byte) faction.value);
        bw.writeDescArray(immutableSlots);
        bw.writeGUID(0); //never used but for safety
    }

    @Override
    public String toString()
    {
        return "ShipCard{" +
                "shipObjectKey=" + shipObjectKey +
                ", level=" + level +
                ", hangarId=" + hangarId +
                ", maxLevel=" + maxLevel +
                ", levelRequirement=" + levelRequirement +
                ", durability=" + durability +
                ", tier=" + tier +
                ", shipRoles=" + Arrays.toString(shipRoles) +
                ", shipRoleDeprecated=" + shipRoleDeprecated +
                ", paperdollUiLayoutfile='" + paperdollUiLayoutfile + '\'' +
                ", shipSlotCards=" + Arrays.toString(shipSlotCards) +
                ", cubitOnlyRepair=" + cubitOnlyRepair +
                ", variantHangarIDs=" + variantHangarIDs +
                ", parentHangarId=" + parentHangarId +
                ", stats=" + stats +
                ", faction=" + faction +
                ", immutableSlots=" + Arrays.toString(immutableSlots) +
                ", nextShipCardGuid=" + nextShipCardGuid +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }


    public long getShipObjectKey()
    {
        return shipObjectKey;
    }

    public byte getLevel()
    {
        return level;
    }

    public byte getHangarId()
    {
        return hangarId;
    }

    public byte getMaxLevel()
    {
        return maxLevel;
    }

    public short getLevelRequirement()
    {
        return levelRequirement;
    }

    public float getDurability()
    {
        return durability;
    }

    public byte getTier()
    {
        return tier;
    }

    public ShipRole[] getShipRoles()
    {
        return shipRoles;
    }

    public ShipRoleDeprecated getShipRoleDeprecated()
    {
        return shipRoleDeprecated;
    }

    public String getPaperdollUiLayoutfile()
    {
        return paperdollUiLayoutfile;
    }

    public ShipSlotCard[] getShipSlotCards()
    {
        return shipSlotCards;
    }
    public Optional<ShipSlotCard> getShipSlotCard(final int slotID)
    {
        return Arrays.stream(this.shipSlotCards)
                .filter(slotCard -> slotCard.getSlotId() == slotID)
                .findFirst();

    }

    public boolean isCubitOnlyRepair()
    {
        return cubitOnlyRepair;
    }

    public List<Long> getVariantHangarIDs()
    {
        return variantHangarIDs;
    }

    public int getParentHangarId()
    {
        return parentHangarId;
    }

    public ObjectStats getStats()
    {
        return stats;
    }

    public Faction getFaction()
    {
        return faction;
    }

    public ShipImmutableSlot[] getImmutableSlots()
    {
        return immutableSlots;
    }

    public long getNextShipCardGuid()
    {
        return nextShipCardGuid;
    }
}
