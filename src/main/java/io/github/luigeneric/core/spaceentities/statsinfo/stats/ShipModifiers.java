package io.github.luigeneric.core.spaceentities.statsinfo.stats;



import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.utils.AbilityActionType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class ShipModifiers
{
    protected final Map<Long, ShipModifier> shipModifiers;
    protected long lastId;

    public ShipModifiers(final Map<Long, ShipModifier> shipModifiers)
    {
        this.shipModifiers = shipModifiers;
    }
    public ShipModifiers()
    {
        this(new HashMap<>());
        this.lastId = 0;
    }

    private long getFreeServerID()
    {
        this.lastId++;
        return this.lastId;
    }


    public Map<ObjectStat, Float> filterForBestRemoteBuffAdd()
    {
        final Map<ObjectStat, Float> bestBuffs = new HashMap<>();
        final Map<ObjectStat, Float> bestDebuffs = new HashMap<>();

        for (ShipModifier modifier : this.shipModifiers.values())
        {
            final ObjectStats remoteBuffAdd = modifier.getRemoteBuffAdd();
            for (Map.Entry<ObjectStat, Float> stat : remoteBuffAdd.getAllStats().entrySet())
            {
                final float value = stat.getValue();
                final boolean isBuff = value > 0;

                final Map<ObjectStat, Float> mapToUse = isBuff ? bestBuffs : bestDebuffs;
                if (mapToUse.containsKey(stat.getKey()))
                {
                    final float currentValue = mapToUse.get(stat.getKey());
                    final float resultValue = filterBuffOrDebuff(isBuff, currentValue, value);
                    mapToUse.put(stat.getKey(), resultValue);
                }
                else
                {
                    mapToUse.put(stat.getKey(), value);
                }
            }
        }

        //merge
        for (final Map.Entry<ObjectStat, Float> debuffStat : bestDebuffs.entrySet())
        {
            // not present, ez merge
            if (!bestBuffs.containsKey(debuffStat.getKey()))
            {
                bestBuffs.put(debuffStat.getKey(), debuffStat.getValue());
            }
            final float bestBuffV = bestBuffs.get(debuffStat.getKey());
            final float bestDebuffV = bestDebuffs.get(debuffStat.getKey());
            final float newValue = bestBuffV + bestDebuffV;
            bestBuffs.put(debuffStat.getKey(), newValue);
        }

        return bestBuffs;
    }
    private float filterBuffOrDebuff(final boolean isBuff, final float oldValue, final float newValue)
    {
        return isBuff ? Mathf.max(oldValue, newValue) : Mathf.min(oldValue, newValue);
    }
    public Map<ObjectStat, Float> filterForBestRemoteBuffMultiply()
    {
        final Map<ObjectStat, Float> bestBuffs = new HashMap<>();
        final Map<ObjectStat, Float> bestDebuffs = new HashMap<>();

        for (final ShipModifier modifier : this.shipModifiers.values())
        {
            final ObjectStats remoteBuffMultiply = modifier.getRemoteBuffMultiply();
            for (Map.Entry<ObjectStat, Float> stat : remoteBuffMultiply.getAllStats().entrySet())
            {
                //determine if debuff or buff value
                final float value = stat.getValue();
                final boolean isBuff = value > 1.0f;

                if (isBuff)
                {

                    if (bestBuffs.containsKey(stat.getKey()))
                    {
                        final float currentValue = bestBuffs.get(stat.getKey());
                        final float bestValue = Mathf.max(currentValue, value);
                        bestBuffs.put(stat.getKey(), bestValue);
                    }
                    else
                    {
                        bestBuffs.put(stat.getKey(), stat.getValue());
                    }
                }
                else
                {
                    if (bestDebuffs.containsKey(stat.getKey()))
                    {
                        final float currentValue = bestDebuffs.get(stat.getKey());
                        final float bestValue = Mathf.min(currentValue, value);
                        bestDebuffs.put(stat.getKey(), bestValue);
                    }
                    else
                    {
                        bestDebuffs.put(stat.getKey(), stat.getValue());
                    }
                }
            }
        }

        //merge
        for (final Map.Entry<ObjectStat, Float> debuffStat : bestDebuffs.entrySet())
        {
            // not present, ez merge
            if (!bestBuffs.containsKey(debuffStat.getKey()))
            {
                bestBuffs.put(debuffStat.getKey(), debuffStat.getValue());
            }
            final float bestBuffV = bestBuffs.get(debuffStat.getKey());
            final float bestDebuffV = bestDebuffs.get(debuffStat.getKey());
            final float newValue = (bestBuffV + bestDebuffV) * 0.5f;
            bestBuffs.put(debuffStat.getKey(), newValue);
        }

        return bestBuffs;
    }


    public List<ShipModifier> addShipModifier(final ShipModifier shipModifier)
    {
        //check if Modifier of this type is already present
        final Optional<ShipModifier> optPresentModifier = this.shipModifiers.values()
                .stream()
                .filter(mod -> mod.getShipSystem().getShipSystemCard().getCardGuid() ==
                        shipModifier.getShipSystem().getShipSystemCard().getCardGuid())
                .findAny();
        long freeID;
        if (optPresentModifier.isPresent())
        {
            this.removeModifier(optPresentModifier.get().getServerID());
            freeID = optPresentModifier.get().getServerID();
        }
        else
        {
            freeID = this.getFreeServerID();
        }

        shipModifier.setServerID(freeID);
        this.shipModifiers.put(freeID, shipModifier);

        return List.of(shipModifier);
    }

    public ShipModifier getShipBuff(long id)
    {
        return this.shipModifiers.get(id);
    }

    public void reset()
    {
        this.shipModifiers.clear();
    }

    public List<ShipModifier> getAll()
    {
        return new ArrayList<>(this.shipModifiers.values());
    }

    public List<ShipModifier> getOfType(final AbilityActionType abilityActionType)
    {
        return this.shipModifiers.values().stream()
                .filter(buff -> buff.getShipAbility().getShipAbilityCard().getAbilityActionType() == abilityActionType)
                .toList();
    }
    public Stream<ShipModifier> getOfTypeStream(final AbilityActionType abilityActionType)
    {
        return this.shipModifiers.values().stream()
                .filter(buff -> buff.getShipAbility().getShipAbilityCard().getAbilityActionType() == abilityActionType);
    }

    public Set<Long> checkTimeout()
    {
        final Set<Long> timeOutedModifiers = new HashSet<>();
        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

        for (final ShipModifier mod : this.shipModifiers.values())
        {
            final float duration = mod.getItemBuffAdd().getStat(ObjectStat.Duration);

            final LocalDateTime lastTimeUsedPlusDuration = mod.getTimeOfActivationLocalDateTime()
                    .plus((long) (duration * 1000), ChronoUnit.MILLIS);

            if (now.isAfter(lastTimeUsedPlusDuration))
            {
                //this.shipBuffs.remove(buff.getServerID());
                timeOutedModifiers.add(mod.getServerID());
            }
        }
        return timeOutedModifiers;
    }

    public void removeModifier(final long serverID)
    {
        this.shipModifiers.remove(serverID);
    }
    public void removeModifier(final Set<Long> toRemoveServerIDs)
    {
        toRemoveServerIDs.forEach(this::removeModifier);
    }

    @Override
    public String toString()
    {
        return "ShipBuffs{" +
                "shipBuffs=" + shipModifiers +
                '}';
    }
}
