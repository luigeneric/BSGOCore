package io.github.luigeneric.core.sector.management.abilities.actions;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.ShipAbility;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.visitors.HoldVisitor;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.relation.Relation;
import io.github.luigeneric.core.sector.management.relation.RelationUtil;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.templates.cards.RegulationCard;
import io.github.luigeneric.templates.cards.ShipAbilityCard;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ShipAbilityAffect;
import io.github.luigeneric.templates.utils.ShipAbilityTargetTier;
import io.github.luigeneric.templates.utils.ShipConsumableOption;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class AbilityAction
{
    /**
     * Ability base infos
     */
    protected final Ship castingShip;
    protected final SpaceSubscribeInfo casterStats;
    protected final ShipSlot castingSlot;
    protected final ShipAbility ability;
    protected final ShipSystem system;
    protected final List<SpaceObject> targetSpaceObjects;
    protected final boolean isAutoCastAbility;
    protected final RegulationCard regulationCard;

    protected final SectorContext ctx;
    protected long startTimeStamp;
    protected float cooldown;
    protected float ppCost;
    protected long diff;


    protected AbilityAction(final Ship castingShip, final ShipSlot castingSlot,
                            final List<SpaceObject> targetSpaceObjects, final boolean isAutoCastAbility,
                            final SectorContext ctx)
    {
        this.castingShip = castingShip;
        this.casterStats = castingShip.getSpaceSubscribeInfo();
        this.castingSlot = castingSlot;
        this.ability = castingSlot.getShipAbility();
        this.system = castingSlot.getShipSystem();
        this.targetSpaceObjects = targetSpaceObjects;
        this.isAutoCastAbility = isAutoCastAbility;
        this.ctx = ctx;
        this.regulationCard = ctx.blueprint().sectorCards().regulationCard();
    }

    /**
     * Calculates every pre information
     *  cooldown, energy, selected mode AND targetsize == 1
     * @return true if and only if everything until there was "okay" which means, every pre condition is satisfied
     */
    protected boolean preFun()
    {
        this.startTimeStamp = ctx.tick().getTimeStamp();
        this.cooldown = ability.getItemBuffAdd().getStat(ObjectStat.Cooldown);
        this.ppCost = ability.getItemBuffAdd().getStat(ObjectStat.PowerPointCost);
        final long lastUsedPlusCooldown = (long) ((system.getTimeOfLastUse() + this.cooldown) * 1000);
        this.diff = startTimeStamp - lastUsedPlusCooldown;

        if (diff < 0)
        {
            return false;
        }

        if (this.casterStats.getPp() < this.ppCost)
        {
            return false;
        }
        if (!this.checkConsumablesSatisfied(castingShip, castingSlot))
        {
            return false;
        }
        if (ability.getShipAbilityCard().getShipAbilityAffect() == ShipAbilityAffect.Selected)
        {
            if (targetSpaceObjects.size() > 1)
            {
                log.warn("Cheat, selected target size is higher than 1! cheaterID: {} size: {}",
                        castingShip.getPlayerId(), targetSpaceObjects.size());
                return false;
            }
        }

        if (castingShip instanceof PlayerShip playerShip)
        {
            playerShip.getPlayerVisibility().finishGhostJumpInIfNotFinished();
        }

        return true;
    }
    protected void postFun()
    {
        this.processConsumables(castingShip, castingSlot);
        system.setTimeOfLastUse(this.startTimeStamp);
        casterStats.setPp(casterStats.getPp() - ppCost);
        this.sendPlayerCastAndShipSlots();
    }

    public void process()
    {
        final boolean everythingOkay = preFun();
        if (!everythingOkay)
            return;

        final boolean internalProcessOkay = internalProcess();
        if (!internalProcessOkay)
            return;
        postFun();
    }

    /**
     * Processes the Class spezific action
     * @return true if and only if everything through the complete process was satisfied
     */
    protected abstract boolean internalProcess();


    private boolean checkConsumablesSatisfied(final Ship castingShip, final ShipSlot shipSlot)
    {
        if (!castingShip.isPlayer()) return true;

        final ShipAbility ability = shipSlot.getShipAbility();
        final ShipAbilityCard abilityCard = ability.getShipAbilityCard();
        if (abilityCard.getShipConsumableOption() != ShipConsumableOption.Using)
            return true;

        final PlayerShip playerShip = (PlayerShip) castingShip;
        final Optional<User> optUser = this.ctx.users().getUser(playerShip.getPlayerId());
        if (optUser.isEmpty()) return false;

        final User client = optUser.get();
        final Optional<ItemCountable> result = client.getPlayer().getHold().hasItemCountable(shipSlot.getCurrentConsumable().getItemCountable());
        if (result.isPresent())
        {
            final ItemCountable countable = result.get();
            return (countable.getCount() > 0);
        }
        return false;
    }
    private void processConsumables(final Ship castingShip, final ShipSlot shipSlot)
    {
        //no player, dont consume
        if (!castingShip.isPlayer())
            return;

        final ShipAbility ability = shipSlot.getShipAbility();
        final ShipAbilityCard abilityCard = ability.getShipAbilityCard();
        //not using type, dont consume anything
        if (abilityCard.getShipConsumableOption() != ShipConsumableOption.Using)
            return;

        final int type = abilityCard.getConsumableType();

        final PlayerShip playerShip = (PlayerShip) castingShip;
        final Optional<User> optUser = this.ctx.users().getUser(playerShip.getPlayerId());
        if (optUser.isEmpty())
            return;

        final User client = optUser.get();
        final Player player = client.getPlayer();
        final Optional<ItemCountable> result = player.getHold().hasItemCountable(shipSlot.getCurrentConsumable().getItemCountable());
        if (result.isEmpty())
            return;
        final ItemCountable currentItemConsumable = result.get();
        HoldVisitor holdVisitor = new HoldVisitor(client, null);
        holdVisitor.reduceItemCountableByCount(currentItemConsumable, 1, player.getHold());
    }

    /**
     * Sends Playercast and SlotStats update
     */
    private void sendPlayerCastAndShipSlots()
    {
        //if no player, stop here
        if (!this.castingShip.isPlayer())
            return;
        final PlayerShip playerShip = (PlayerShip) this.castingShip;
        final Optional<User> optUser = this.ctx.users().getUser(playerShip.getPlayerId());
        if (optUser.isEmpty())
            return;

        final User user = optUser.get();

        final GameProtocol gameProtocol = user.getProtocol(ProtocolID.Game);
        final PlayerProtocol playerProtocol = user.getProtocol(ProtocolID.Player);

        ctx.sender().sendToClient(gameProtocol.writer().writeCast(this.system.getServerID()), user);

        final HangarShip activeShip = user.getPlayer().getHangar().getActiveShip();
        ctx.sender().sendToClient(playerProtocol.writer().writeShipSlots(activeShip), user);
    }

    protected boolean targetTierCheck(final SpaceObject target)
    {
        //if no ship, it's allowed
        if (!(target instanceof final Ship ship))
        {
            return true;
        }
        final Set<ShipAbilityTargetTier> targetTiers = this.ability.getShipAbilityCard().getTargetTiers();

        if (targetTiers.isEmpty())
            return false;

        if (targetTiers.contains(ShipAbilityTargetTier.Any))
            return true;

        //because of the check above, this is first ship!
        final ShipAbilityTargetTier targetTier = ability.getShipAbilityCard().tierToEnum(ship.getShipCard().getTier());
        return targetTiers.contains(targetTier);
    }
    protected Relation getRelation(final SpaceObject target)
    {
        return RelationUtil.getRelation(this.castingShip, target, this.regulationCard.getTargetBracketMode());
    }
}
