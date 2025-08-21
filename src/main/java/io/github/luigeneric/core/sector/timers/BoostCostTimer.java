package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.Hold;
import io.github.luigeneric.core.player.container.visitors.HoldVisitor;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.cards.SectorCard;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class BoostCostTimer extends DelayedTimer
{
    private final SectorUsers sectorUsers;
    private final SectorCard sectorCard;
    public BoostCostTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks, SectorUsers sectorUsers,
                          final SectorCard sectorCard)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.sectorUsers = sectorUsers;
        this.sectorCard = sectorCard;
    }

    @Override
    protected void delayedUpdate()
    {
        for (final PlayerShip playerShip : sectorUsers.getPlayerShipsCollection())
        {
            if (playerShip.getMovementController().getMovementOptions().getGear() != Gear.Boost)
                continue;

            //it's in gear boost => reduce tylium
            final Optional<User> optUser = sectorUsers.getUser(playerShip.getPlayerId());
            if (optUser.isEmpty())
                continue;
            final User user = optUser.get();
            final Hold hold = user.getPlayer().getHold();
            final HoldVisitor holdVisitor = new HoldVisitor(user);
            final Optional<ShipItem> optTylium = hold.getByGUID(ResourceType.Tylium.guid);
            if (optTylium.isEmpty())
                continue;
            final ItemCountable tylium = (ItemCountable) optTylium.get();
            final long boostCosts = (long) Math.ceil(playerShip.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.BoostCost));
            try
            {
                holdVisitor.reduceItemCountableByCount(tylium, boostCosts, hold);
                user.getPlayer().getCounterFacade()
                        .incrementCounter(CounterCardType.tylium_burned, sectorCard.getCardGuid(), boostCosts);
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                log.error(user.getUserLog() + "Cannot reduce boost, not enough tylium " + user.getPlayer().getPlayerLog());
                playerShip.getMovementController().getMovementOptions().setGear(Gear.Regular);
                playerShip.getMovementController().setMovementOptionsNeedUpdate();
            }
        }
    }
}
