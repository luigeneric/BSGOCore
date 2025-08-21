package io.github.luigeneric.templates.cards;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.ConsumableEffectType;
import io.github.luigeneric.templates.utils.TargetBracketMode;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RegulationCard extends Card
{
    private final Map<Long, Set<Integer>> abilityTargetRelations;
    private final Map<Long, Set<Integer>> abilityTargetTypes;
    private final TargetBracketMode targetBracketMode;
    private final boolean sectorMapEnabled;
    @SerializedName("effectTypeBlacklist")
    private final ConsumableEffectType[] effectTypeBlacklist;
    public RegulationCard(long cardGuid, Map<Long, Set<Integer>> abilityTargetRelations,
                          Map<Long, Set<Integer>> abilityTargetTypes,
                          TargetBracketMode targetBracketMode, boolean sectorMapEnabled, ConsumableEffectType[] effectTypeBlacklist)
    {
        super(cardGuid, CardView.Regulation);
        this.abilityTargetRelations = abilityTargetRelations;
        this.abilityTargetTypes = abilityTargetTypes;
        this.targetBracketMode = targetBracketMode;
        this.sectorMapEnabled = sectorMapEnabled;
        this.effectTypeBlacklist = effectTypeBlacklist;
    }

    public RegulationCard(long cardGuid, Map<Long, Set<Integer>> abilityTargetRelations,
                          Map<Long, Set<Integer>> abilityTargetTypes, ConsumableEffectType[] effectTypeBlacklist)
    {
        this(cardGuid, abilityTargetRelations, abilityTargetTypes, TargetBracketMode.Default, true, effectTypeBlacklist);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(targetBracketMode.getValue());
        bw.writeBoolean(sectorMapEnabled);

        Set<Long> keys = abilityTargetRelations.keySet();
        bw.writeUInt16(keys.size());
        for (long key : keys)
        {
            bw.writeUInt32(key);
            int value1 = 0;
            Set<Integer> abilitySides = abilityTargetRelations.get(key);
            for (int abilityside : abilitySides)
            {
                value1 |= abilityside;
            }
            int value2 = 0;
            Set<Integer> abilityTargets = abilityTargetTypes.get(key);
            for (int abilityTarget : abilityTargets)
            {
                value2 |= abilityTarget;
            }
            bw.writeUInt16(value1);
            bw.writeUInt16(value2);
        }

        int effectBlacklistSize = effectTypeBlacklist.length;
        bw.writeUInt16(effectBlacklistSize);
        for (int i = 0; i < effectBlacklistSize; i++)
        {
            //could be enhanced with writeBytes() but before that make it work...
            bw.writeByte(effectTypeBlacklist[i].getValue());
        }
    }

    @Override
    public String toString()
    {
        return "RegulationCard{" +
                "abilityTargetRelations=" + abilityTargetRelations +
                ", abilityTargetTypes=" + abilityTargetTypes +
                ", targetBracketMode=" + targetBracketMode +
                ", sectorMapEnabled=" + sectorMapEnabled +
                ", effectTypeBlacklist=" + Arrays.toString(effectTypeBlacklist) +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public Map<Long, Set<Integer>> getAbilityTargetRelations()
    {
        return abilityTargetRelations;
    }

    public Map<Long, Set<Integer>> getAbilityTargetTypes()
    {
        return abilityTargetTypes;
    }

    public TargetBracketMode getTargetBracketMode()
    {
        return targetBracketMode;
    }

    public boolean isSectorMapEnabled()
    {
        return sectorMapEnabled;
    }

    public ConsumableEffectType[] getEffectTypeBlacklist()
    {
        return effectTypeBlacklist;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegulationCard that = (RegulationCard) o;

        if (sectorMapEnabled != that.sectorMapEnabled) return false;
        if (!Objects.equals(abilityTargetRelations, that.abilityTargetRelations))
            return false;
        if (!Objects.equals(abilityTargetTypes, that.abilityTargetTypes))
            return false;
        if (targetBracketMode != that.targetBracketMode) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(effectTypeBlacklist, that.effectTypeBlacklist);
    }

    @Override
    public int hashCode()
    {
        int result = 0;
        result = 31 * result + (abilityTargetRelations != null ? abilityTargetRelations.hashCode() : 0);
        result = 31 * result + (abilityTargetTypes != null ? abilityTargetTypes.hashCode() : 0);
        result = 31 * result + (targetBracketMode != null ? targetBracketMode.hashCode() : 0);
        result = 31 * result + (sectorMapEnabled ? 1 : 0);
        result = 31 * result + Arrays.hashCode(effectTypeBlacklist);
        return result;
    }
}
