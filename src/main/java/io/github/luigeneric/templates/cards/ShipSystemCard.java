package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.*;

import java.util.Set;

public class ShipSystemCard extends Card
{
    @SerializedName("Level")
    private final byte level;
    @SerializedName("MaxLevel")
    private final byte maxLevel;
    @SerializedName("nextShipSystemCardGuid")
    private final long nextCardGuid; //set to 0 if no next cardguid
    //private final ShipSlotType shipSlotType;
    @SerializedName("SlotType")
    private final ShipSlotType shipSlotType;

    @SerializedName("Tier")
    private final byte tier;
    @SerializedName("ShipObjectKeyRestrictions")
    private final Set<Long> shipObjectKeyRestrictions;
    @SerializedName("ShipRoleRestrictions")
    private final ShipRole[] shipRoleRestrictions;
    //private final ShipRole[] shipRoleRestrictions;
    @SerializedName("SkillHashes")
    private final long[] skillHashes;
    @SerializedName("shipAbilityCards")
    private final long[] shipAbilityCards;
    @SerializedName("StaticBuffs")
    private final ObjectStats staticBuffs;
    @SerializedName("MultiplyBuffs")
    private final ObjectStats multiplyBuffs;
    @SerializedName("Durability")
    private final float durability;
    @SerializedName("Class")
    private final ShipSystemClass shipSystemClass;
    //private final byte shipSystemClass;

    @SerializedName("Views")
    private final StatView[] statViews;
    //private final byte[] statViews;
    @SerializedName("Unique")
    private final boolean unique;
    @SerializedName("ReplaceableOnly")
    private final boolean replaceableOnly;
    @SerializedName("UserUpgradeable")
    private final boolean userUpgradeable;
    @SerializedName("Trashable")
    private final boolean trashable;
    @SerializedName("Indestructible")
    private final boolean indestructible;
    @SerializedName("MaxCountPerShip")
    private final short maxCountPerShip;


    public ShipSystemCard(long cardGuid, byte level, byte maxLevel, long nextCardGuid, ShipSlotType shipSlotType, byte tier, Set<Long> shipObjectKeyRestrictions,
                          ShipRole[] shipRoleRestrictions, long[] skillHashes, long[] shipAbilityCards, ObjectStats staticBuffs,
                          ObjectStats multiplyBuffs, float durability, ShipSystemClass shipSystemClass, StatView[] statViews, boolean unique,
                          boolean replaceableOnly, boolean userUpgradeable, boolean trashable, boolean indestructible, short maxCountPerShip)
    {
        super(cardGuid, CardView.ShipSystem);
        this.level = level;
        this.maxLevel = maxLevel;
        this.nextCardGuid = nextCardGuid;
        this.shipSlotType = shipSlotType;
        this.tier = tier;
        this.shipObjectKeyRestrictions = shipObjectKeyRestrictions;
        this.shipRoleRestrictions = shipRoleRestrictions;
        this.skillHashes = skillHashes;
        this.shipAbilityCards = shipAbilityCards;
        this.staticBuffs = staticBuffs;
        this.multiplyBuffs = multiplyBuffs;
        this.durability = durability;
        this.shipSystemClass = shipSystemClass;
        this.statViews = statViews;
        this.unique = unique;
        this.replaceableOnly = replaceableOnly;
        this.userUpgradeable = userUpgradeable;
        this.trashable = trashable;
        this.indestructible = indestructible;
        this.maxCountPerShip = maxCountPerShip;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(level);
        bw.writeByte(maxLevel);
        bw.writeGUID(nextCardGuid);
        bw.writeByte(shipSlotType.getValue());
        bw.writeByte(tier);

        bw.writeUInt32Collection(shipObjectKeyRestrictions);

        bw.writeUInt16(shipRoleRestrictions.length);
        for (ShipRole shipRole : shipRoleRestrictions)
        {
            bw.writeByte(shipRole.value);
        }

        //bw.writeUInt32List(Arrays.asList(skillHashes));
        bw.writeUint32Array(skillHashes);

        bw.writeUint32Array(shipAbilityCards);

        bw.writeDesc(staticBuffs);
        bw.writeDesc(multiplyBuffs);
        bw.writeSingle(durability);
        bw.writeByte(shipSystemClass.value);

        bw.writeUInt16(statViews.length);
        for(StatView statView : statViews)
        {
            bw.writeByte(statView.getValue());
        }

        bw.writeBoolean(unique);
        bw.writeBoolean(replaceableOnly);
        bw.writeBoolean(userUpgradeable);
        bw.writeBoolean(trashable);
        bw.writeBoolean(indestructible);
        bw.writeByte((byte) maxCountPerShip);
    }


    public byte getLevel()
    {
        return level;
    }

    public byte getMaxLevel()
    {
        return maxLevel;
    }

    public long getNextCardGuid()
    {
        return nextCardGuid;
    }

    public ShipSlotType getShipSlotType()
    {
        return shipSlotType;
    }

    public byte getTier()
    {
        return tier;
    }

    public Set<Long> getShipObjectKeyRestrictions()
    {
        return shipObjectKeyRestrictions;
    }
    public boolean isObjectKeyRestrictionsBlocked(final long shipObjectKey)
    {
        return shipObjectKeyRestrictions.size() > 0 && !shipObjectKeyRestrictions.contains(shipObjectKey);
    }

    public ShipRole[] getShipRoleRestrictions()
    {
        return shipRoleRestrictions;
    }

    public long[] getSkillHashes()
    {
        return skillHashes;
    }

    public long[] getShipAbilityCards()
    {
        return shipAbilityCards;
    }

    public ObjectStats getStaticBuffs()
    {
        return staticBuffs;
    }

    public ObjectStats getMultiplyBuffs()
    {
        return multiplyBuffs;
    }

    public float getDurability()
    {
        return durability;
    }

    public ShipSystemClass getShipSystemClass()
    {
        return shipSystemClass;
    }

    public StatView[] getStatViews()
    {
        return statViews;
    }

    public boolean isUnique()
    {
        return unique;
    }

    public boolean isReplaceableOnly()
    {
        return replaceableOnly;
    }

    public boolean isUserUpgradeable()
    {
        return userUpgradeable;
    }

    public boolean isTrashable()
    {
        return trashable;
    }

    public boolean isIndestructible()
    {
        return indestructible;
    }

    public short getMaxCountPerShip()
    {
        return maxCountPerShip;
    }

    @Override
    public String toString()
    {
        return "ShipSystemCard{" +
                "level=" + level +
                ", maxLevel=" + maxLevel +
                ", shipSlotType=" + shipSlotType +
                ", tier=" + tier +
                ", cardGuid=" + cardGuid +
                '}';
    }
}
