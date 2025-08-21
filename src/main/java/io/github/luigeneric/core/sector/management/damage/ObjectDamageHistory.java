package io.github.luigeneric.core.sector.management.damage;

import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.ShipModifiers;

import java.util.*;
import java.util.function.Predicate;

public class ObjectDamageHistory
{
    /**
     * Key = DamageDealer SpaceObject-ID
     * Value = new Object if null or empty
     */
    private float sumDamage;
    private final Map<Long, AccumulatedDamage> damageMap;
    private final Set<AccumulatedDamage> sortedByDamage;
    private final Deque<DamageRecord> damageRecords;
    private final static int MAX_RECORDS = 1_000;
    private final SectorUsers sectorUsers;

    public ObjectDamageHistory(final Map<Long, AccumulatedDamage> damageMap, final Set<AccumulatedDamage> sortedByDamage,
                               final Deque<DamageRecord> damageRecords, final SectorUsers sectorUsers)
    {
        this.damageMap = damageMap;
        this.sortedByDamage = sortedByDamage;
        this.damageRecords = damageRecords;
        this.sectorUsers = sectorUsers;
    }

    public ObjectDamageHistory(final SectorUsers sectorUsers)
    {
        this(new HashMap<>(), new TreeSet<>(), new ArrayDeque<>(), sectorUsers);
    }

    public boolean hasHighestDamageDealer()
    {
        return !this.sortedByDamage.isEmpty();
    }


    /**
     * update the who and what by each damage done if changed
     */
    public void damageReceived(final DamageRecord damageRecord)
    {
        AccumulatedDamage history = this.damageMap.get(damageRecord.from().getObjectID());
        if (history == null)
        {
            history = new AccumulatedDamage(damageRecord.from(), damageRecord.timeStamp(), damageRecord.damage(), damageRecord.isKillShot());
            this.damageMap.put(damageRecord.from().getObjectID(), history);
            this.sortedByDamage.add(history);
        } else
        {
            this.sortedByDamage.remove(history);
            history.update(damageRecord.timeStamp(), damageRecord.damage(), damageRecord.isKillShot());
            this.sortedByDamage.add(history);
        }
        this.sumDamage += damageRecord.damage();
        addRecord(damageRecord);

        //add damage based on buffs or debuffs
        Optional<ShipModifiers> optModsDealer = damageRecord.from().getSpaceSubscribeInfo().getModifiers();
        Optional<ShipModifiers> optModsReceiver = damageRecord.to().getSpaceSubscribeInfo().getModifiers();

        /**
         * There is dmg done based on buffs and debuffs
         *  a) buffs: the dealer received help from a friendly player FP, FP receives buff dmg
         *  b) debuffs: the dealer received help in dealing dmg on the target because the target got debuffed,
         *      the Debuffer needs to receive the dmg counted as debuff dmg
         */


        //buffs of the dmg dealer
        if (optModsDealer.isPresent())
        {
            //find the playerids of the buffers the dealer received
            final ShipModifiers modsDealer = optModsDealer.get();
            final List<Long> dealerModsPlayerIds = modsDealer.getAll()
                    .stream()
                    .map(ShipModifier::getSourcePlayerId)
                    .toList();
            //find the history of the dealer(playerids) for the buff
            incrementDmgHistoryOfEachPlayerForModifier(damageRecord, dealerModsPlayerIds, true);
        }
        if (optModsReceiver.isPresent())
        {
            final ShipModifiers modsOnShipDmgDealedOn = optModsReceiver.get();
            final List<Long> receiverModsPlayerIds = modsOnShipDmgDealedOn.getAll()
                    .stream()
                    .map(ShipModifier::getSourcePlayerId)
                    .toList();
            incrementDmgHistoryOfEachPlayerForModifier(damageRecord, receiverModsPlayerIds, false);
        }
    }

    private void incrementDmgHistoryOfEachPlayerForModifier(final DamageRecord damageRecord, final List<Long> modPlayerId, final boolean isBuff)
    {
        for (final long playerId : modPlayerId)
        {
            final Optional<PlayerShip> optPlayerShip = sectorUsers.getPlayerShipByUserID(playerId);
            if (optPlayerShip.isEmpty())
                continue;


            //dealer must be the same faction as the modifier user
            if (damageRecord.from().getFaction() == optPlayerShip.get().getFaction())
            {
                final PlayerShip playerShip = optPlayerShip.get();
                final AccumulatedDamage accumulatedDamage = damageMap
                        .computeIfAbsent(playerShip.getObjectID(), k ->
                        {
                            return new AccumulatedDamage(playerShip, damageRecord.timeStamp(), 0, false);
                        });
                accumulatedDamage.updateModifierDmg(damageRecord.from(), damageRecord.damage(), isBuff);
            }
        }
    }

    private void addRecord(final DamageRecord damageRecord)
    {
        if (this.damageRecords.size() >= MAX_RECORDS)
        {
            this.damageRecords.removeFirst();
        }
        this.damageRecords.add(damageRecord);
    }

    public void removeDamageDealer(final AccumulatedDamage accumulatedDamage)
    {
        var resultMap = this.damageMap.remove(accumulatedDamage.getDealer().getObjectID());
        var resultSet = this.sortedByDamage.remove(accumulatedDamage);
    }


    public Optional<AccumulatedDamage> getHighestDamageDealer()
    {
        final Iterator<AccumulatedDamage> iterator = sortedByDamage.iterator();
        if (!iterator.hasNext())
            return Optional.empty();

        return Optional.of(iterator.next());
    }

    public Optional<AccumulatedDamage> getKillShotDealer()
    {
        for (final AccumulatedDamage accumulatedDamage : this.sortedByDamage)
        {
            if (accumulatedDamage.isKillShot())
                return Optional.of(accumulatedDamage);
        }
        return Optional.empty();
    }
    public boolean isDead()
    {
        final DamageRecord record = this.damageRecords.peek();
        if (record == null)
            return false;

        return record.isKillShot();
    }
    public DamageRecord getLastDamage()
    {
        return this.damageRecords.peekLast();
    }
    public DamageRecord getLastDamageByPlayer()
    {
        final Iterator<DamageRecord> desc = this.damageRecords.descendingIterator();
        while (desc.hasNext())
        {
            final DamageRecord item = desc.next();
            if (item.from().isPlayer())
                return item;
        }
        return null;
    }
    public boolean hasLastDamage()
    {
        return this.damageRecords.peek() != null;
    }

    /**
     * Provides a set of the damage dealers sorted by the highest damage done.
     * Only provides actual damage dealers
     * @return a Set of the damage dealers based on their actual damage sorted desc
     */
    public Set<AccumulatedDamage> getSortedByDamage()
    {
        return sortedByDamage;
    }

    public Optional<AccumulatedDamage> getByObjectID(final long id)
    {
        return Optional.ofNullable(this.damageMap.get(id));
    }

    public float getSumDamage()
    {
        return sumDamage;
    }

    public List<AccumulatedDamage> getAll(Predicate<AccumulatedDamage> predicate)
    {
        if (predicate == null)
            return damageMap.values().stream().toList();
        return this.damageMap.values().stream().filter(predicate).toList();
    }
}
