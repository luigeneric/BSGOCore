package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.AugmentActionType;
import io.github.luigeneric.templates.utils.ConsumableEffectType;
import io.github.luigeneric.templates.utils.ObjectStats;

import java.util.Arrays;

public class ShipConsumableCard extends Card
{
    @SerializedName("ConsumableType")
    private final int consumableType;
    @SerializedName("Tier")
    private final byte tier;
    @SerializedName("ItemBuffMultiply")
    private final ObjectStats itemBuffMultiply;
    @SerializedName("ItemBuffAdd")
    private final ObjectStats itemBuffAdd;
    @SerializedName("Action")
    private final AugmentActionType augmentActionType;
    @SerializedName("IsAugment")
    private final boolean isAugment;
    @SerializedName("AutoConsume")
    private final boolean autoConsume;
    @SerializedName("Trashable")
    private final boolean trashable;
    @SerializedName("buyCount")
    private final int buyCount;
    private final String[] consumableAttributes;
    private final ConsumableEffectType effectType;

    public ShipConsumableCard(long cardGUID, int consumableType, byte tier, ObjectStats itemBuffMultiply,
                              ObjectStats itemBuffAdd, AugmentActionType augmentActionType, boolean isAugment,
                              boolean autoConsume, boolean trashable, int buyCount, String[] consumableAttributes, ConsumableEffectType effectType)
    {
        super(cardGUID, CardView.ShipConsumable);
        this.consumableType = consumableType;
        this.tier = tier;
        this.itemBuffMultiply = itemBuffMultiply;
        this.itemBuffAdd = itemBuffAdd;
        this.augmentActionType = augmentActionType;
        this.isAugment = isAugment;
        this.autoConsume = autoConsume;
        this.trashable = trashable;
        this.buyCount = buyCount;
        this.consumableAttributes = consumableAttributes;
        this.effectType = effectType;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt16(consumableType);
        bw.writeByte(tier);
        bw.writeDesc(itemBuffMultiply);
        bw.writeDesc(itemBuffAdd);
        bw.writeByte(augmentActionType.value);
        bw.writeBoolean(isAugment);
        bw.writeBoolean(autoConsume);
        bw.writeBoolean(trashable);
        bw.writeUInt16(buyCount);
        bw.writeStringArray(consumableAttributes);
        bw.writeByte(effectType.getValue());
    }

    @Override
    public String toString()
    {
        return "ShipConsumableCard{" +
                "consumableType=" + consumableType +
                ", tier=" + tier +
                ", itemBuffMultiply=" + itemBuffMultiply +
                ", itemBuffAdd=" + itemBuffAdd +
                ", augmentActionType=" + augmentActionType +
                ", isAugment=" + isAugment +
                ", autoConsume=" + autoConsume +
                ", trashable=" + trashable +
                ", buyCount=" + buyCount +
                ", consumableAttributes=" + Arrays.toString(consumableAttributes) +
                ", effectType=" + effectType +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public int getConsumableType()
    {
        return consumableType;
    }

    public byte getTier()
    {
        return tier;
    }

    public ObjectStats getItemBuffMultiply()
    {
        return itemBuffMultiply;
    }

    public ObjectStats getItemBuffAdd()
    {
        return itemBuffAdd;
    }

    public AugmentActionType getAugmentActionType()
    {
        return augmentActionType;
    }

    public boolean isAugment()
    {
        return isAugment;
    }

    public boolean isAutoConsume()
    {
        return autoConsume;
    }

    public boolean isTrashable()
    {
        return trashable;
    }

    public int getBuyCount()
    {
        return buyCount;
    }

    public String[] getConsumableAttributes()
    {
        return consumableAttributes;
    }

    public ConsumableEffectType getEffectType()
    {
        return effectType;
    }
}
