package io.github.luigeneric.core.sector.management.damage;

import io.github.luigeneric.core.gameplayalgorithms.DamageCalcStrategy;
import io.github.luigeneric.core.player.ShipAbility;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.utils.BgoRandom;

public class DamageCalculator
{
    private final SectorAlgorithms sectorAlgorithms;
    private final BgoRandom bgoRandom;
    private final Tick tick;
    private final DamageCalcStrategy damageCalcStrategy;

    public DamageCalculator(
            final SectorAlgorithms sectorAlgorithms,
            final Tick tick,
            final DamageCalcStrategy damageCalcStrategy
    )
    {
        this.sectorAlgorithms = sectorAlgorithms;
        this.tick = tick;
        this.damageCalcStrategy = damageCalcStrategy;
        this.bgoRandom = new BgoRandom();
    }


    public DamageRecord calculateDamageMining(final SpaceObject from, final SpaceObject enemyShip, final ShipAbility ability)
    {
        final ObjectStats itemBuffAddStats = ability.getItemBuffAdd();

        final float armorPiercing = itemBuffAddStats.getStatOrDefault(ObjectStat.ArmorPiercing);
        final float criticalAttack = itemBuffAddStats.getStatOrDefault(ObjectStat.CriticalOffense);
        final float damageMining = enemyShip.getSpaceEntityType().isOfType(SpaceEntityType.Asteroid, SpaceEntityType.Comet) ?
                itemBuffAddStats.getStatOrDefault(ObjectStat.DamageMining) : 1f;
        final float dmgLow = itemBuffAddStats.getStatOrDefault(ObjectStat.DamageLow) * damageMining;
        final float dmgHigh = itemBuffAddStats.getStatOrDefault(ObjectStat.DamageHigh) * damageMining;

        return this.calculateDamage(from, enemyShip, armorPiercing, criticalAttack, dmgLow, dmgHigh);
    }


    public DamageRecord calculateDamageFromMissile(final Missile missile, final SpaceObject collisionShip)
    {
        final SpaceSubscribeInfo missileStats = missile.getSpaceSubscribeInfo();
        final float armorPiercing = missileStats.getStatOrDefault(ObjectStat.ArmorPiercing);
        final float criticalAttack = missileStats.getStatOrDefault(ObjectStat.CriticalOffense);
        final float dmgLow = missileStats.getStatOrDefault(ObjectStat.DamageLow);
        final float dmgHigh = missileStats.getStatOrDefault(ObjectStat.DamageHigh);

        return this.calculateDamage(missile.getOwnerObject(), collisionShip, armorPiercing, criticalAttack, dmgLow, dmgHigh);
    }
    public DamageRecord calculateDamage(final SpaceObject from, final SpaceObject to, final ShipAbility ability)
    {
        final ObjectStats itemBuffAdd = ability.getItemBuffAdd();

        final float armorPiercing = itemBuffAdd.getStatOrDefault(ObjectStat.ArmorPiercing);
        final float criticalAttack = itemBuffAdd.getStatOrDefault(ObjectStat.CriticalOffense);
        final float dmgLow = itemBuffAdd.getStatOrDefault(ObjectStat.DamageLow);
        final float dmgHigh = itemBuffAdd.getStatOrDefault(ObjectStat.DamageHigh);

        return this.calculateDamage(from, to, armorPiercing, criticalAttack, dmgLow, dmgHigh);
    }
    private DamageRecord calculateDamage(final SpaceObject damageDoneBy, final SpaceObject enemyObject,
                                         final float armorPiercing, final float criticalAttack,
                                         final float dmgLow, final float dmgHigh)
    {
        final SpaceSubscribeInfo enemyShipStats = enemyObject.getSpaceSubscribeInfo();

        float armor = enemyShipStats.getStatOrDefault(ObjectStat.ArmorValue, 0f);
        float criticalDef = enemyShipStats.getStatOrDefault(ObjectStat.CriticalDefense, 0f);


        final float armorMultiplicator = this.sectorAlgorithms.getArmorAlgorithm().getMultiplicator(armor, armorPiercing);
        final float criticalChance = this.sectorAlgorithms.getCritchanceAlgorithm().getCritChance(criticalAttack, criticalDef);
        final float baseDmg = damageCalcStrategy.getDamageRoll(bgoRandom, dmgLow, dmgHigh);

        final boolean isCrit = this.bgoRandom.rollChance(criticalChance);
        final float critMult = sectorAlgorithms.getCritchanceAlgorithm().getOverallCriticalMultiplier(isCrit);

        return new DamageRecord(damageDoneBy, enemyObject, baseDmg * critMult * armorMultiplicator, isCrit, this.tick.getTimeStamp());
    }

    public DamageRecord calculateDamageFromCollision(final Asteroid asteroid, final SpaceObject other)
    {
        final float currentHP = asteroid.getSpaceSubscribeInfo()
                .getStatOrDefault(ObjectStat.MaxHullPoints, asteroid.getSpaceSubscribeInfo().getHp());
        final float armor = other.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.ArmorValue);
        final float mult = this.sectorAlgorithms.getArmorAlgorithm().getMultiplicator(armor, 50);
        final float finalDmg = 0.5f * currentHP * mult;

        return new DamageRecord(asteroid, other, finalDmg, false, this.tick.getTimeStamp());
    }
}
