package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.*;

import java.util.List;
import java.util.Set;


public class ShipAbilityCard extends Card
{
    @SerializedName("Level")
    private final short level;
    @SerializedName("Launch")
    private final ShipAbilityLaunch shipAbilityLaunch;
    @SerializedName("Affect")
    private final ShipAbilityAffect shipAbilityAffect;
    @SerializedName("AbilityGroupId")
    private final long abilityGroupId;
    @SerializedName("TargetTiers")
    private final Set<ShipAbilityTargetTier> targetTiers;
    @SerializedName("ConsumableType")
    private final int consumableType;
    @SerializedName("ConsumableTier")
    private final long consumableTier;
    @SerializedName("ConsumableOption")
    private final ShipConsumableOption shipConsumableOption;
    @SerializedName("ActionType")
    private final AbilityActionType abilityActionType;
    @SerializedName("OverwriteActionType")
    private final AbilityActionType overwriteActionType;
    @SerializedName("GUIBuffAtlas")
    private final String guiBuffAtlas;
    @SerializedName("GUIBuffIndex")
    private final int guiBuffIndex;
    @SerializedName("ItemBuffAdd")
    private final ObjectStats itemBuffAdd;
    @SerializedName("ItemBuffMultiply")
    private final ObjectStats itemBuffMultiply;
    @SerializedName("RemoteBuffAdd")
    private final ObjectStats remoteBuffAdd;
    @SerializedName("RemoteBuffMultiply")
    private final ObjectStats remoteBuffMultiply;
    @SerializedName("ToggleSystemAdd")
    private final ObjectStats toggleSystemAdd;
    @SerializedName("ToggleSystemMultiply")
    private final ObjectStats getToggleSystemMultiply;
    @SerializedName("OnByDefault")
    private final boolean onByDefault;
    @SerializedName("effectTypeBlacklist")
    private final List<ConsumableEffectType> consumableEffectTypesBlacklist;
    @SerializedName("AffectedAbilityTypes")
    private final Set<AbilityActionType> abilityActionTypes;


    public ShipAbilityCard(long cardGuid, CardView view, short level, ShipAbilityLaunch shipAbilityLaunch,
                           ShipAbilityAffect shipAbilityAffect, long abilityGroupId, Set<ShipAbilityTargetTier> targetTiers,
                           int consumableType, long consumableTier, ShipConsumableOption shipConsumableOption,
                           AbilityActionType abilityActionType, AbilityActionType overwriteActionType, String guiBuffAtlas,
                           int guiBuffIndex, ObjectStats itemBuffAdd, ObjectStats itemBuffMultiply, ObjectStats remoteBuffAdd,
                           ObjectStats remoteBuffMultiply, ObjectStats toggleSystemAdd, ObjectStats getToggleSystemMultiply,
                           boolean onByDefault, List<ConsumableEffectType> consumableEffectTypesBlacklist,
                           Set<AbilityActionType> abilityActionTypes)
    {
        super(cardGuid, view);
        this.level = level;
        this.shipAbilityLaunch = shipAbilityLaunch;
        this.shipAbilityAffect = shipAbilityAffect;
        this.abilityGroupId = abilityGroupId;
        this.targetTiers = targetTiers;
        this.consumableType = consumableType;
        this.consumableTier = consumableTier;
        this.shipConsumableOption = shipConsumableOption;
        this.abilityActionType = abilityActionType;
        this.overwriteActionType = overwriteActionType;
        this.guiBuffAtlas = guiBuffAtlas;
        this.guiBuffIndex = guiBuffIndex;
        this.itemBuffAdd = itemBuffAdd;
        this.itemBuffMultiply = itemBuffMultiply;
        this.remoteBuffAdd = remoteBuffAdd;
        this.remoteBuffMultiply = remoteBuffMultiply;
        this.toggleSystemAdd = toggleSystemAdd;
        this.getToggleSystemMultiply = getToggleSystemMultiply;
        this.onByDefault = onByDefault;
        this.consumableEffectTypesBlacklist = consumableEffectTypesBlacklist;
        this.abilityActionTypes = abilityActionTypes;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeByte((byte) level);
        bw.writeByte(shipAbilityLaunch.value);
        bw.writeByte(shipAbilityAffect.value);
        bw.writeUInt32(abilityGroupId);

        int toWrite = 0;
        for (ShipAbilityTargetTier item : targetTiers)
        {
            toWrite |= item.value;
        }
        bw.writeUInt16(toWrite);

        bw.writeUInt16(consumableType);
        bw.writeUInt32(consumableTier);
        bw.writeByte(shipConsumableOption.value);
        bw.writeByte(abilityActionType.value());
        bw.writeByte(overwriteActionType.value());
        bw.writeString(guiBuffAtlas);
        bw.writeUInt16(guiBuffIndex);
        bw.writeDesc(itemBuffAdd);
        bw.writeDesc(itemBuffMultiply);
        bw.writeDesc(remoteBuffAdd);
        bw.writeDesc(remoteBuffMultiply);
        bw.writeDesc(toggleSystemAdd);
        bw.writeDesc(getToggleSystemMultiply);
        bw.writeBoolean(onByDefault);

        int blackListSize = consumableEffectTypesBlacklist.size();
        bw.writeUInt16(blackListSize);
        for (int i = 0; i < blackListSize; i++)
        {
            bw.writeByte(consumableEffectTypesBlacklist.get(i).getValue());
        }
        int affectAbilityTypesSize = abilityActionTypes.size();
        bw.writeUInt16(affectAbilityTypesSize);
        for (AbilityActionType type : abilityActionTypes)
        {
            bw.writeByte(type.value());
        }
    }

    public short getLevel()
    {
        return level;
    }

    public ShipAbilityLaunch getShipAbilityLaunch()
    {
        return shipAbilityLaunch;
    }

    public ShipAbilityAffect getShipAbilityAffect()
    {
        return shipAbilityAffect;
    }

    public long getAbilityGroupId()
    {
        return abilityGroupId;
    }

    public Set<ShipAbilityTargetTier> getTargetTiers()
    {
        return targetTiers;
    }

    public int getConsumableType()
    {
        return consumableType;
    }

    public long getConsumableTier()
    {
        return consumableTier;
    }

    public ShipConsumableOption getShipConsumableOption()
    {
        return shipConsumableOption;
    }

    public AbilityActionType getAbilityActionType()
    {
        return abilityActionType;
    }

    public AbilityActionType getOverwriteActionType()
    {
        return overwriteActionType;
    }

    public String getGuiBuffAtlas()
    {
        return guiBuffAtlas;
    }

    public int getGuiBuffIndex()
    {
        return guiBuffIndex;
    }

    public ObjectStats getItemBuffAdd()
    {
        return itemBuffAdd;
    }

    public ObjectStats getItemBuffMultiply()
    {
        return itemBuffMultiply;
    }

    public ObjectStats getRemoteBuffAdd()
    {
        return remoteBuffAdd;
    }

    public ObjectStats getRemoteBuffMultiply()
    {
        return remoteBuffMultiply;
    }

    public ObjectStats getToggleSystemAdd()
    {
        return toggleSystemAdd;
    }

    public ObjectStats getGetToggleSystemMultiply()
    {
        return getToggleSystemMultiply;
    }

    public boolean isOnByDefault()
    {
        return onByDefault;
    }

    public List<ConsumableEffectType> getConsumableEffectTypesBlacklist()
    {
        return consumableEffectTypesBlacklist;
    }

    public Set<AbilityActionType> getAbilityActionTypes()
    {
        return abilityActionTypes;
    }

    public ShipAbilityTargetTier tierToEnum(final byte tier)
    {
        final int rv = 1 << tier - 1;
        return ShipAbilityTargetTier.forValue(rv);
    }
}
