package io.github.luigeneric.core.spaceentities.statsinfo.stats;

import io.github.luigeneric.core.player.ShipAbility;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.templates.cards.ShipConsumableCard;
import io.github.luigeneric.templates.cards.ShipSystemCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.utils.AbilityActionType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.utils.Utils;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ShipSubscribeInfo extends SpaceSubscribeInfo
{
    protected final ObjectStats statsBase;
    protected final ObjectStats statsWithSlots;
    protected final ShipModifiers shipModifiers;
    protected final CombatInfo combatInfo;
    protected final AtomicLong targetObjectID;
    protected ShipSlots shipSlots;
    protected final Catalogue catalogue;

    public ShipSubscribeInfo(Owner ownerID, ObjectStats statsFinal, float hullPoints, float powerPoints, ShipModifiers shipModifiers, CombatInfo combatInfo,
                             final AtomicLong targetObjectID)
    {
        super(ownerID, statsFinal.getCopy(), hullPoints, powerPoints);
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.statsBase = statsFinal.getCopy();
        this.statsWithSlots = statsFinal.getCopy();
        this.shipModifiers = shipModifiers;
        this.combatInfo = combatInfo;
        this.targetObjectID = targetObjectID;
    }
    public ShipSubscribeInfo(final long ownerID, final ObjectStats stats)
    {
        this(new Owner(ownerID, false), stats, 1f, 1f,
                new ShipModifiers(), new CombatInfo(), new AtomicLong(0));
    }

    @Override
    public void setShipSlots(final ShipSlots shipSlots)
    {
        Objects.requireNonNull(shipSlots, "ShipSlots cannot be null");
        this.shipSlots = shipSlots;
    }

    protected void resetStats()
    {
        super.resetStats();
        this.statsFinal.put(this.statsBase.getCopy());
        this.statsWithSlots.put(this.statsBase.getCopy());
    }

    protected void applySlotSystemStats()
    {
        if (this.shipSlots == null) return;
        for (final ShipSlot slot : this.shipSlots.values())
        {
            final ShipSystemCard shipSystemCard = slot.getShipSystem().getShipSystemCard();
            if (shipSystemCard == null)
                continue;

            ObjectStats staticBuffs = shipSystemCard.getStaticBuffs();
            staticBuffs = ObjectStats.mapObjectStats(staticBuffs);
            //this.applyStatsAdd(staticBuffs);
            ObjectStats.applyStatsAddTo(staticBuffs, this.statsWithSlots);

            ObjectStats multBuffs = shipSystemCard.getMultiplyBuffs();
            multBuffs = ObjectStats.mapObjectStats(multBuffs);
            //this.applyStatsMult(multBuffs);
            ObjectStats.applyStatsMultTo(multBuffs, this.statsWithSlots);
        }
    }

    public void applyAbilityStats()
    {
        this.getShipSlots().ifPresent(slots ->
        {
            for (final ShipSlot slot : slots.values())
            {
                if (slot.getShipSystem() != null && slot.getShipAbility() != null)
                {
                    //probably implement detection for
                    this.applyAbilitySlotStats(slot);

                    //cams system stats on abilities
                    for (ShipSlot internalSlots : slots.values())
                    {
                        if (internalSlots.getShipSystem() == null || internalSlots.getShipSystem().getShipSystemCard() == null)
                        {
                            continue;
                        }

                        var ability = slot.getShipAbility();
                        var system = internalSlots.getShipSystem();
                        var systemCard = system.getShipSystemCard();
                        var systemMultsOnGuns = systemCard.getMultiplyBuffs();
                        switch (ability.getShipAbilityCard().getAbilityActionType())
                        {
                            case FireCannon ->
                            {
                                if (systemMultsOnGuns.containsStat(ObjectStat.CannonAngle))
                                {
                                    var oldAngle = ability.getItemBuffAdd().getStat(ObjectStat.Angle);
                                    var currentAngle = oldAngle * systemMultsOnGuns.getStat(ObjectStat.CannonAngle);
                                    ability.getItemBuffAdd().setStat(ObjectStat.Angle, currentAngle);
                                }
                            }
                            case FireMining ->
                            {
                                if (systemMultsOnGuns.containsStat(ObjectStat.MiningAngle))
                                {
                                    var oldAngle = ability.getItemBuffAdd().getStat(ObjectStat.Angle);
                                    var currentAngle = oldAngle * systemMultsOnGuns.getStat(ObjectStat.MiningAngle);
                                    ability.getItemBuffAdd().setStat(ObjectStat.Angle, currentAngle);
                                }
                            }
                        }
                    }


                    for (final BasePropertyBuffer value : this.subscribers.values())
                    {
                        value.onSlotUpdate(this, slot.getShipSystem().getServerID());
                    }
                }
            }
        });
    }

    @Override
    public void applyStats()
    {
        super.applyStats();             //reset
        this.applySlotSystemStats();    //slots
        this.applyAbilityStats();
        this.statsFinal.put(this.statsWithSlots);
        applyFromModBonus();
        this.applyModifiers();          //slots + modifiers
        for (final BasePropertyBuffer buffer : this.subscribers.values())
        {
            buffer.onStatInfoChanged(this, StatInfo.Stats);
        }
        this.getMovementUpdateSubscriber().ifPresent(ud -> ud.setMovementOptionsStats(this));
    }


    protected void applyModifiers()
    {
        final ObjectStats bestBuffRemoteModifiers = new ObjectStats(this.shipModifiers.filterForBestRemoteBuffAdd());
        ObjectStats.applyStatsAddTo(bestBuffRemoteModifiers, this.statsFinal);


        final ObjectStats bestBestModifiers = new ObjectStats(this.shipModifiers.filterForBestRemoteBuffMultiply());
        final ObjectStats statsMultiplyBonus = ObjectStats.getStatsMultiplyBonus(this.statsWithSlots, bestBestModifiers);
        ObjectStats.applyStatsAddTo(statsMultiplyBonus, this.statsFinal);
    }

    public void applyAbilitySlotStats(final ShipSlot slot)
    {
        if (slot == null)
        {
            log.warn("applyAbilitySlotStats but slot was null!");
            return;
        }
        final ShipAbility ability = slot.getShipAbility();
        ability.resetStats();
        final ItemCountable currentConsumable = slot.getCurrentConsumable().getItemCountable();

        this.getSkillBook().ifPresent(skillBook ->
        {
            final ObjectStats multSkillStats = skillBook.mapToObjectStats(ability.getShipAbilityCard().getAbilityActionType());
            ObjectStats.applyStatsMultTo(multSkillStats, ability.getItemBuffAdd());
        });

        final ObjectStats baseStats = ability.getItemBuffAdd().getCopy();
        final ObjectStats consumableBonusStats = new ObjectStats();

        //consumableBonus = baseStats * consumableStats
        if (currentConsumable.getCardGuid() != 0)
        {
            final ShipConsumableCard shipConsumableCard = Utils.fetchShipConsumableCard(currentConsumable);
            final ObjectStats tmpBonus = ObjectStats.applyStatsMultToIfBonusExistsInApplyOn(shipConsumableCard.getItemBuffAdd(), baseStats);
            consumableBonusStats.setStats(tmpBonus);
        }

        this.getShipModifiers().ifPresent(modifiers ->
        {
            ///TODO add add stats

            // ----- multipliers -----
            final Map<ObjectStat, Float> best = modifiers.filterForBestRemoteBuffMultiply();
            ObjectStats buffs = new ObjectStats(best);
            if (ability.getShipAbilityCard().getAbilityActionType().equals(AbilityActionType.FireMissle))
            {
                buffs.removeStat(ObjectStat.BoostSpeed);
                buffs.removeStat(ObjectStat.TurnSpeed);
                buffs.removeStat(ObjectStat.Speed);
            }
            ObjectStats.applyStatsMultTo(buffs, baseStats);
            ability.getItemBuffAdd().setStats(baseStats);
        });
        if (!consumableBonusStats.getAllStats().isEmpty())
            ObjectStats.applyStatsAddTo(consumableBonusStats, ability.getItemBuffAdd());
    }

    @Override
    protected Optional<CombatInfo> getCombatInfo()
    {
        return Optional.of(this.combatInfo);
    }

    @Override
    public Optional<AtomicLong> getTargetObjectID()
    {
        return Optional.of(this.targetObjectID);
    }

    @Override
    public Optional<ShipSlots> getShipSlots()
    {
        return Optional.ofNullable(this.shipSlots);
    }

    @Override
    protected Optional<ShipModifiers> getShipModifiers()
    {
        return Optional.ofNullable(this.shipModifiers);
    }

    @Override
    public Optional<ShipModifiers> getModifiers()
    {
        return Optional.ofNullable(this.shipModifiers);
    }
}
