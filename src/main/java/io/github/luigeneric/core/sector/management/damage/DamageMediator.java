package io.github.luigeneric.core.sector.management.damage;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.ShipAbility;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.management.lootsystem.claims.LootClaimHolder;
import io.github.luigeneric.core.spaceentities.*;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.CounterCardType;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;


@Slf4j
public class DamageMediator
{
    private final SectorContext ctx;
    private final DamageCalculator damageCalculator;
    private final LootClaimHolder lootClaimHolder;
    private final ISpaceObjectRemover remover;
    private final SectorDamageHistory sectorDamageHistory;
    private final DamageDurabilityModifier damageDurabilityModifier;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;
    private final SectorCards sectorCards;

    public DamageMediator(final SectorContext ctx,
                          final DamageCalculator damageCalculator,
                          final LootClaimHolder lootClaimHolder,
                          final ISpaceObjectRemover remover,
                          final SectorDamageHistory sectorDamageHistory,
                          final DamageDurabilityModifier damageDurabilityModifier,
                          final GameProtocolWriteOnly gameProtocolWriteOnly
    )
    {
        this.ctx = ctx;
        this.damageCalculator = damageCalculator;
        this.lootClaimHolder = lootClaimHolder;
        this.remover = remover;
        this.sectorDamageHistory = sectorDamageHistory;
        this.damageDurabilityModifier = damageDurabilityModifier;
        this.gameProtocolWriteOnly = gameProtocolWriteOnly;
        this.sectorCards = ctx.blueprint().sectorCards();
    }

    public void dealDamage(final DamageRecord damageRecord)
    {
        final float damage = damageRecord.damage();

        final Optional<RemovingCause> removingCause = damageRecord.to().getRemovingCause();
        removingCause.ifPresent(cause ->
        {
            log.error("in deal damage, object to kill is already removed, objType={}, oldRemovingCause={}",
                    damageRecord.to().getSpaceEntityType(),
                    cause
            );
        });

        final SpaceSubscribeInfo toStats = damageRecord.to().getSpaceSubscribeInfo();
        float damageDealtCleaned = damage;
        boolean wasKillShot = false;

        final float targetHP = toStats.getHp();
        final float newHPDirty = targetHP - damage;
        if (newHPDirty < 0)
        {
            damageDealtCleaned = targetHP;
        }

        toStats.setHp(Mathf.max(newHPDirty, 0f));
        if (toStats.getHp() == 0f)
        {
            this.remover.notifyRemovingCauseAdded(damageRecord.to(), RemovingCause.Death, damageRecord.from());
            toStats.setPp(0);
            wasKillShot = true;
        }

        /*
        PrometheusMetrics.INSTANCE.getDamageDealtTotal()
                .labels(damageRecord.from().getSpaceEntityType().name(), damageRecord.from().getFaction().name())
                .inc(damageDealtCleaned);
        PrometheusMetrics.INSTANCE.getDamageReceivedTotal()
                .labels(damageRecord.to().getSpaceEntityType().name(), damageRecord.to().getFaction().name())
                .inc(damageDealtCleaned);
         */
        final DamageRecord newDmgRecord = new DamageRecord(damageRecord, damageDealtCleaned, wasKillShot);
        this.sectorDamageHistory.damageUpdate(newDmgRecord);
        this.damageDurabilityModifier.damageReceived(newDmgRecord);
        sendDealDamageTo(newDmgRecord);
    }

    private void sendDealDamageTo(final DamageRecord damageRecord)
    {
        final SpaceObject castingShip = damageRecord.from();
        final SpaceObject targetShip = damageRecord.to();
        final boolean isCritical = damageRecord.isCritical();
        final boolean isDestroyed = damageRecord.to().isRemoved();
        final float damage = damageRecord.damage();

        if (castingShip.isPlayer())
        {
            final PlayerShip castingPlayerShip = (PlayerShip) castingShip;
            final Optional<User> optUser = ctx.users().getUser(castingPlayerShip.getPlayerId());
            if (optUser.isPresent())
            {
                final User client = optUser.get();
                client.send(gameProtocolWriteOnly.writeCombatInfo(true, targetShip.getObjectID(),
                        damageRecord.damage(), isDestroyed, isCritical));
                client.getPlayer().getCounterFacade().incrementCounter(
                        CounterCardType.damage_dealt,
                        sectorCards.sectorCard().getCardGuid(),
                        damage
                );
                if (targetShip.getSpaceEntityType() == SpaceEntityType.Outpost)
                {
                    client.getPlayer().getCounterFacade().incrementCounter(
                            CounterCardType.outposts_damage_dealt,
                            sectorCards.sectorCard().getCardGuid(),
                            damage
                    );
                }
                try
                {
                    this.lootClaimHolder.updateClaim(damageRecord, client);
                }
                catch (final Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        if (targetShip.isPlayer())
        {
            final PlayerShip targetPlayerShip = (PlayerShip) targetShip;
            final Optional<User> optUser = ctx.users().getUser(targetPlayerShip);
            if (optUser.isPresent())
            {
                final User otherClient = optUser.get();
                otherClient.send(gameProtocolWriteOnly.writeCombatInfo(false, castingShip.getObjectID(),
                        damageRecord.damage(), isDestroyed, isCritical));
                otherClient.getPlayer().getCounterFacade().incrementCounter(
                        CounterCardType.damage_received,
                        sectorCards.sectorCard().getCardGuid(),
                        damage
                );
                if (castingShip.getSpaceEntityType() == SpaceEntityType.Outpost)
                {
                    otherClient.getPlayer().getCounterFacade().incrementCounter(
                            CounterCardType.outposts_damage_received,
                            sectorCards.sectorCard().getCardGuid(),
                            damage
                    );
                }
            }

        }

        if (targetShip.getFaction() != Faction.Neutral)
        {
            castingShip.getSpaceSubscribeInfo().setLastCombatTime(ctx.tick().getTimeStamp());
            targetShip.getSpaceSubscribeInfo().setLastCombatTime(ctx.tick().getTimeStamp());
        }
    }

    public void dealDamageFromAsteroidCollision(final Asteroid asteroid, final SpaceObject to)
    {
        final DamageRecord res = this.damageCalculator.calculateDamageFromCollision(asteroid, to);
        this.dealDamage(res);
    }
    public void dealDamageFromMissile(final Missile missile, final SpaceObject to)
    {
        final DamageRecord res = this.damageCalculator.calculateDamageFromMissile(missile, to);
        this.dealDamage(res);
    }
    public void dealDamageFromAbility(final SpaceObject from, final SpaceObject to, final ShipAbility ability)
    {
        final DamageRecord res = this.damageCalculator.calculateDamage(from, to, ability);
        this.dealDamage(res);
    }

    public void dealDamageFromMining(final Ship castingShip, final SpaceObject target, final ShipAbility ability)
    {
        final DamageRecord res = this.damageCalculator.calculateDamageMining(castingShip, target, ability);
        this.dealDamage(res);
    }
}
