package io.github.luigeneric.core.sector.management;

import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.core.player.container.visitors.HoldVisitor;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.lootsystem.claims.LootClaimHolder;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.utils.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Slf4j
public class MiningSectorOperations
{
    private final SectorSpaceObjects spaceObjects;
    private final LootClaimHolder lootClaimHolder;
    private final SectorJoinQueue sectorJoinQueue;
    private final SpaceObjectFactory spaceObjectFactory;
    private final SectorCards sectorCards;
    private final Map<Long, List<MiningRequests>> userIdToMiningRequests;
    private final Duration maxTimeBetweenMiningRequestAndCurrentTime;
    private final Lock lock;

    public MiningSectorOperations(
            final SectorSpaceObjects spaceObjects,
            final LootClaimHolder lootClaimHolder,
            final SectorJoinQueue sectorJoinQueue,
            final SpaceObjectFactory spaceObjectFactory,
            final SectorCards sectorCards
    )
    {
        this(
                spaceObjects, lootClaimHolder, sectorJoinQueue, spaceObjectFactory, sectorCards,
                new HashMap<>(), Duration.ofMinutes(10), new ReentrantLock()
        );
    }

    public void addMiningRequest(final long userId, final long objectId, final Price price)
    {
        lock.lock();
        try
        {
            this.userIdToMiningRequests.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(MiningRequests.ofObjectIdAndPrice(objectId, price));
        }
        finally
        {
            lock.unlock();
        }
    }

    public Optional<Price> removeMiningRequest(final long userId, final long objectId)
    {
        lock.lock();
        try
        {
            List<MiningRequests> miningKeys = this.userIdToMiningRequests.get(userId);
            Optional<MiningRequests> keyWasPresent = miningKeys.stream().filter(k -> k.objectId == objectId).findAny();
            if (keyWasPresent.isEmpty())
            {
                return Optional.empty();
            }
            final MiningRequests miningRequests = keyWasPresent.get();

            final boolean isTsFine = isTimestampFine(BgoTimeStamp.now(), miningRequests);
            if (isTsFine)
            {
                miningKeys.remove(miningRequests);
                return Optional.of(miningRequests.price());
            } else
            {
                return Optional.empty();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private boolean isTimestampFine(final BgoTimeStamp now, final MiningRequests miningRequest)
    {
        final Duration duration = now.getDuration(miningRequest.miningRequestTimeStamp());
        // check if duration is less than maxTimeBetweenMiningRequestAndCurrentTime
        return  (duration.compareTo(maxTimeBetweenMiningRequestAndCurrentTime) < 0);
    }

    public void clearOld()
    {
        lock.lock();
        final BgoTimeStamp now = BgoTimeStamp.now();
        try
        {
            for (Map.Entry<Long, List<MiningRequests>> longListEntry : userIdToMiningRequests.entrySet())
            {
                longListEntry
                        .getValue()
                        .removeIf(miningRequests ->
                                isTimestampFine(now, miningRequests)
                        );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void onMiningShipRequest(final User user, final long objectID, final Price price)
    {
        final Optional<SpaceObject> optPlanetoid = this.spaceObjects.get(objectID);
        if (optPlanetoid.isEmpty())
            return;
        final SpaceObject planetoid = optPlanetoid.get();
        if (planetoid.getSpaceEntityType() != SpaceEntityType.Planetoid)
        {
            log.error("MiningShip call on non planetoid object!!! " + user.getUserLog());
            return;
        }
        final Planetoid tmpPlanetoid = (Planetoid) planetoid;
        if (tmpPlanetoid.hasMiningShip())
        {
            log.info("MiningShip call, MiningShip already present! " + tmpPlanetoid.getObjectID() + " " + user.getUserLog());
            return;
        }
        final Optional<Loot> optLoot = lootClaimHolder.getLootAssociations().get(tmpPlanetoid);
        if (optLoot.isEmpty())
        {
            log.error("MiningShip call, MiningShip has no loot! " + user.getUserLog());
            return;
        }

        final boolean isEnoughPriceToBuy = ContainerVisitor.isEnoughInContainer(price, user.getPlayer().getHold(), 1);
        if (!isEnoughPriceToBuy)
        {
            log.error("Price not enough to buy " + user.getUserLog());
            return;
        }

        final HoldVisitor holdVisitor = new HoldVisitor(user, null);
        holdVisitor.removeBuyResources(price, user.getPlayer().getHold(), 1);

        final SpaceObject newMiningShip = this.spaceObjectFactory.createMiningShip(user, (Planetoid) planetoid);
        final Player player = user.getPlayer();
        player.getCounterFacade().incrementCounter(CounterCardType.mining_ships_called, sectorCards.sectorCard().getCardGuid());
        this.sectorJoinQueue.addSpaceObject(newMiningShip);
    }


    record MiningRequests(long objectId, Price price, BgoTimeStamp miningRequestTimeStamp) {
        public static MiningRequests ofObjectIdAndPrice(final long objectId, final Price price)
        {
            return new MiningRequests(objectId, price, BgoTimeStamp.now());
        }
    }
}
