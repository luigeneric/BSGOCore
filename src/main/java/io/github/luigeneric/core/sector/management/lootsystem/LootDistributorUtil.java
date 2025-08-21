package io.github.luigeneric.core.sector.management.lootsystem;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.player.factors.Factors;
import io.github.luigeneric.core.protocols.debug.DebugProtocolWriteOnly;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.management.damage.AccumulatedDamage;
import io.github.luigeneric.core.sector.management.damage.ObjectDamageHistory;
import io.github.luigeneric.core.sector.management.lootsystem.claims.PvpClaim;
import io.github.luigeneric.core.sector.management.lootsystem.killtrace.PvpKillHistory;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.templates.loot.LootDamageRadiusTemplate;
import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.loot.LootTemplate;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.utils.BgoRandom;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LootDistributorUtil
{
    private LootDistributorUtil(){}


    public static List<PvpResult> determinePvpResults(final SectorUsers sectorUsers, final UsersContainer usersContainer,
                                                      final PvpClaim pvpClaim, final SpaceObject objectDied,
                                                      final List<ItemCountableLootEntryInfo> rolledItems,
                                                      final long experience, final Tick tick, final GameServerParamsConfig gameServerParamsConfig,
                                                      final PvpKillHistory pvpKillHistory
    )
    {
        //find the clame or throw it away if empty
        final Optional<SpaceObject> optClaimObj = pvpClaim.getClaimObject();
        if (optClaimObj.isEmpty())
        {
            return List.of();
        }
        final List<PvpResult> pvpResults = new ArrayList<>();


        final float maxHp = objectDied.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints);

        //get most damage
        final SpaceObject claimObj = optClaimObj.get();
        final Optional<User> optPlayer = sectorUsers.getUser(claimObj);
        optPlayer.ifPresent(player -> pvpResults.add(new PvpResult(player, rolledItems, experience, SpecialAction.AssistCountingAsKill)));

        //get killshot dealer
        final Optional<AccumulatedDamage> optKillShot = pvpClaim.getKillShotObject();
        if (optKillShot.isPresent())
        {
            final AccumulatedDamage killShot = optKillShot.get();
            if (!killShot.getDealer().equals(claimObj))
            {
                final boolean damageHighEnough = (maxHp * pvpClaim.getMinimumPercentageDamage()) < killShot.getAccumulatedDamage();
                final Optional<User> optKillShotUser = sectorUsers.getUser(killShot.getDealer());
                optKillShotUser.ifPresent(killShotUser ->
                {
                    if (damageHighEnough)
                    {
                        pvpResults.add(new PvpResult(killShotUser, rolledItems, experience, SpecialAction.Killer));
                    }
                });
            }
        }
        //get all assisting objects
        final Set<AccumulatedDamage> sortedByDmg = pvpClaim.getSortedByDmg();
        for (final AccumulatedDamage assistObject : sortedByDmg)
        {
            final SpaceObject assistingDealer = assistObject.getDealer();
            if (assistingDealer.equals(claimObj))
            {
                continue;
            }

            if (optKillShot.isPresent())
            {
                final SpaceObject killShotdealer = optKillShot.get().getDealer();
                if (killShotdealer.equals(assistingDealer))
                    continue;
            }
            final boolean damageHighEnough = (maxHp * pvpClaim.getMinimumPercentageDamage()) < assistObject.getAccumulatedDamage();
            if (damageHighEnough)
            {
                final boolean timeStampIsInvalid = (assistObject.getLastTime() + pvpClaim.getClaimTimeUntilFree()) < tick.getTimeStamp();
                if (timeStampIsInvalid)
                {
                    continue;
                }
                final Optional<User> optAssistingUsr = sectorUsers.getUser(assistObject.getDealer());
                optAssistingUsr.ifPresent(assistingUsr ->
                {
                    pvpResults.add(new PvpResult(assistingUsr, rolledItems, experience, SpecialAction.Assist));
                });
            }
        }
        //for each buffer and debuffer
        final List<AccumulatedDamage> allDmgDealers = pvpClaim.getAllDmgDealer(accu -> accu.getDealer().isPlayer());
        for (final AccumulatedDamage accumulatedDamage : allDmgDealers)
        {
            final SpaceObject modifierDealer = accumulatedDamage.getDealer();
            final Optional<User> opdModifierUser = sectorUsers.getUser(modifierDealer);

            final float dmgBuffs = accumulatedDamage.getDmgBasedOnBuffs();
            final float dmgDebuffs = accumulatedDamage.getDmgBasedOnDebuffs();
            final float minDmgNeeded = maxHp * (pvpClaim.getMinimumPercentageDamage() * 2);

            if (dmgBuffs > minDmgNeeded && opdModifierUser.isPresent())
            {
                final Optional<PvpResult> containedPvpResult = pvpResults.stream().filter(result -> result.user().equals(opdModifierUser.get())).findAny();
                //check if the pvp result already contains an item of the buffer
                if (containedPvpResult.isPresent())
                {
                    containedPvpResult.get().specialActions().add(SpecialAction.Buffer);
                }
                else
                {
                    pvpResults.add(new PvpResult(opdModifierUser.get(), rolledItems, experience, new ArrayList<>(List.of(SpecialAction.Buffer))));
                }
            }
            if (dmgDebuffs > minDmgNeeded && opdModifierUser.isPresent())
            {
                final Optional<PvpResult> containedPvpResult = pvpResults.stream().filter(result -> result.user().equals(opdModifierUser.get())).findAny();
                //check if the pvp result already contains an item of the de-buffer
                if (containedPvpResult.isPresent())
                {
                    containedPvpResult.get().specialActions().add(SpecialAction.Debuffer);
                }
                else
                {
                    pvpResults.add(new PvpResult(opdModifierUser.get(), rolledItems, experience, new ArrayList<>(List.of(SpecialAction.Debuffer))));
                }
            }
        }

        /*
            check if one of pvp result users has the same ip addr as the dead user
            - dead user is present
            - dead user connection is present
            - dead user ip
         */
        final Optional<User> optDeadUser = usersContainer.get(objectDied.getPlayerId());
        log.info("dead user is present={}", optDeadUser.isPresent());
        if (optDeadUser.isPresent())
        {
            User deadUser = optDeadUser.get();

            for (final PvpResult pvpResult : pvpResults)
            {
                if (pvpResult.user().isSameIp(deadUser))
                {
                    log.warn("pvp kill, same ip, deathUserLog={}, killUserLog={}", deadUser.getUserLog(), pvpResult.user().getUserLog());
                    if (gameServerParamsConfig.starterParams().testingMode())
                    {
                        var pvpMessageBw = new DebugProtocolWriteOnly().writeMessage("Pvp result same ip!");
                        pvpResult.user().send(pvpMessageBw);
                    }
                }

                //if the pvpResult contains a kill, add it to the pvpkillhistory
                if (pvpResult.containsAction(SpecialAction.Killer, SpecialAction.AssistCountingAsKill))
                {
                    pvpKillHistory.addPvpKilled(
                            pvpResult.user().getPlayer().getUserID(), pvpResult.user().getPlayer().getName(),
                            deadUser.getPlayer().getUserID(), deadUser.getPlayer().getName()
                    );
                }
            }
        }


        return pvpResults;
    }
    public static List<User> getUsersFromPartySameSector(final User user)
    {
        final Optional<IParty> optParty = user.getPlayer().getParty();
        if (optParty.isEmpty())
        {
            if (user.getPlayer().getLocation().getGameLocation() == GameLocation.Space)
                return List.of(user);
            else
                return List.of();
        }
        final IParty party = optParty.get();

        return party.getMembers().stream()
                .filter(member -> member.getPlayer().getLocation().getGameLocation() == GameLocation.Space)
                .filter(member -> member.getPlayer().getLocation().getSectorID() == user.getPlayer().getSectorId())
                .toList();
    }

    public static List<UserLoot> applyFactorsUserItems(final List<UserItems> userItems)
    {
        final List<UserLoot> userLoots = new ArrayList<>(userItems.size());

        for (final UserItems userItem : userItems)
        {
            userLoots.add(applyFactorUserItem(userItem));
        }
        return userLoots;
    }

    public static UserLoot applyFactorUserItem(final UserItems userItems)
    {
        final Factors factors = userItems.user().getPlayer().getFactors();
        final float lootMult = factors.getMultiplierFor(FactorType.Loot);
        final float xpMult = factors.getMultiplierFor(FactorType.Experience);

        final long resultXp = (long) Math.floor(userItems.exp() * xpMult);

        final List<ItemCountableBonusType> itemCountableBonusTypes = new ArrayList<>();
        for (final ItemCountable itemCountable : userItems.itemCountables())
        {
            final ItemCountableBonusType itemBonusWrapper = new ItemCountableBonusType(itemCountable, new HashMap<>());
            if (lootMult > 1)
            {
                final long resultCount = (long) Math.floor(itemCountable.getCount() * lootMult);
                final long increasedBy = resultCount - itemCountable.getCount();
                itemCountable.updateCount(resultCount);

                itemBonusWrapper.lootBonusTypeLongMap().put(LootBonusType.Booster, increasedBy);
            }


            itemCountableBonusTypes.add(itemBonusWrapper);
        }
        return new UserLoot(userItems.user(), resultXp, itemCountableBonusTypes, userItems.specialActions());
    }



    /**
     * TODO fix radius
     * @param lootTemplate
     * @param user
     * @param killedSpaceObject
     * @return
     */
    public static Set<User> getAssociatedLootUsers(final LootTemplate lootTemplate, final User user, final SpaceObject killedSpaceObject,
                                                    final ObjectDamageHistory objectDamageHistory, final SectorUsers sectorUsers)
    {
        final Set<User> associatedUsers = new HashSet<>();

        switch (lootTemplate.getType())
        {
            //only the highestDamage receives loot
            case Damage ->
            {
                final List<User> membersFiltered = getUsersFromPartySameSector(user);
                final List<User> filteredForGlobal = membersFiltered.stream()
                                .filter(member -> lootTemplate.isInGlobalLevel(member.getPlayer().getSkillBook().get()))
                                        .toList();
                associatedUsers.addAll(filteredForGlobal);
            }
            //only inside radius and min damage done
            case RadiusDamage ->
            {
                if (!(lootTemplate instanceof LootDamageRadiusTemplate lootDamageRadiusTemplate))
                {
                    throw new IllegalStateException("RadiusDamage type but not of this type!");
                }
                final Set<AccumulatedDamage> allDamageDealers = objectDamageHistory.getSortedByDamage();
                final Optional<AccumulatedDamage> optHighestDmgDealer = objectDamageHistory.getHighestDamageDealer();
                //if highest dealer is not present, do nothing
                if (optHighestDmgDealer.isEmpty())
                    break;
                Faction factionHighestDealer = optHighestDmgDealer.get().getDealer().getFaction();
                for (AccumulatedDamage allDamageDealer : allDamageDealers)
                {
                    if (allDamageDealer.getDealer().getSpaceEntityType() == SpaceEntityType.Outpost)
                    {
                        associatedUsers.clear();
                        break;
                    }

                    Optional<User> optUser;
                    if (allDamageDealer.getDealer().isPlayer()
                            && (optUser = sectorUsers.getUser(allDamageDealer.getDealer())).isPresent()
                            && lootDamageRadiusTemplate.getRadius() >= (killedSpaceObject.getMovementController().getPosition().distance(allDamageDealer.getDealer().getMovementController().getPosition()))
                            && lootDamageRadiusTemplate.getMinDamage() <= allDamageDealer.getAccumulatedDamage()
                            && allDamageDealer.getDealer().getFaction() == factionHighestDealer
                    )
                    {
                        associatedUsers.add(optUser.get());
                    }
                }
            }
        }

        return associatedUsers;
    }

    public static List<UserItems> updateItemCountBasedOnGroupSize(final List<UserItems> userItemsList, final int groupSize)
    {
        final List<UserItems> returnLst = new ArrayList<>();
        for (final UserItems userItems : userItemsList)
        {

            for (final ItemCountable itemCountable : userItems.itemCountables())
            {
                final long newCount = ceilLootCount((double)itemCountable.getCount() / (double)groupSize);
                itemCountable.updateCount(newCount);
            }
            final long xpDiv = ceilLootCount((double)userItems.exp() / (double) groupSize);
            returnLst.add(new UserItems(userItems.user(), userItems.itemCountables(), xpDiv));
        }
        return returnLst;
    }

    public static List<UserItems> updateItemCountBasedOnSpecialActionModifier(final List<UserItems> userItemsList)
    {
        final List<UserItems> userItemsResultList = new ArrayList<>();
        for (UserItems userItems : userItemsList)
        {
            final List<ItemCountable> items = userItems.itemCountables();
            final List<ItemCountable> newItemsList = new ArrayList<>();
            for (ItemCountable item : items)
            {
                final ItemCountable tmpItem = item.copy();
                tmpItem.updateCount(ceilLootCount(item.getCount() * userItems.highestSpecialAction().lootMultiplier));
                newItemsList.add(tmpItem);
            }
            userItemsResultList.add(
                    new UserItems(
                            userItems.user(),
                            newItemsList,
                            ceilLootCount(userItems.exp() * userItems.highestSpecialAction().lootMultiplier),
                            userItems.specialActions()
                    )
            );
        }
        return userItemsResultList;
    }

    /**
     * 1) count is never lower than 1!
     * 2) if count is not a natural number(integer), round up!
     * @param newCount
     * @return
     */
    private static long ceilLootCount(final double newCount)
    {
        return (long) Math.ceil(Math.max(newCount, 1));
    }

    /**
     * Filter level and faction limitations
     * @param usersOfInterest the users who receive loot
     * @param rolledItems all items which are already rolled
     * @return list of association of user and filtered items
     */
    public static List<UserItems> filterForLevelFactions(final Set<User> usersOfInterest,
                                                         final List<ItemCountableLootEntryInfo> rolledItems,
                                                         final long exp)
    {
        final List<UserItems> userItemsList = new ArrayList<>();
        for (final User user : usersOfInterest)
        {
            userItemsList.add(filterForLevelFaction(user, rolledItems, exp, List.of(SpecialAction.None)));
        }
        return userItemsList;
    }
    public static List<UserItems> filterForLevelFactions(final List<PvpResult> pvpResults)
    {
        final List<UserItems> resultItemsList = new ArrayList<>();
        for (final PvpResult pvpResult : pvpResults)
        {
            final UserItems filtered = filterForLevelFaction(pvpResult.user(), pvpResult.rolledItems(), pvpResult.experience(), pvpResult.specialActions());
            resultItemsList.add(filtered);
        }
        return resultItemsList;
    }

    public static UserItems filterForLevelFaction(
            final User user,
            final List<ItemCountableLootEntryInfo> rolledItems,
            final long exp,
            final List<SpecialAction> specialActions
    )
    {
        final List<ItemCountable> tmpItems = new ArrayList<>();
        for (final ItemCountableLootEntryInfo rolledItem : rolledItems)
        {
            final boolean levelOkay = rolledItem.lootEntryInfo()
                    .isInLevelIntervall(user.getPlayer().getSkillBook().get());

            final boolean factionOkay = rolledItem.lootEntryInfo().allowedToReceiveFaction(user.getPlayer().getFaction());

            if (levelOkay && factionOkay)
            {
                tmpItems.add(rolledItem.itemCountable().copy());
            }
        }
        return new UserItems(user, tmpItems, exp, specialActions);
    }


    /**
     * Rolls based on lootEntries items
     *  1) based on chance
     *  2) based on variation-percentage
     * @param lootTemplate the Template to roll items from
     * @param bgoRandom random gen
     * @return a list of ItemCountables
     */
    public static List<ItemCountableLootEntryInfo> rollItemsFromLootEntries(final LootTemplate lootTemplate, final BgoRandom bgoRandom)
    {
        final List<ItemCountableLootEntryInfo> itemCountables = new ArrayList<>();

        final boolean isInOveralLChance = bgoRandom.rollChance(lootTemplate.getChance());
        if (!isInOveralLChance)
            return List.of();
        for (LootEntryInfo lootEntryInfo : lootTemplate.getLootEntryInfos())
        {
            final boolean rollSuccessfully = bgoRandom.rollChance(lootEntryInfo.chance());
            if (rollSuccessfully)
            {
                if (lootEntryInfo.shipItem() instanceof ItemCountable countable)
                {
                    final long rolledCount = bgoRandom.variateByPercentage(countable.getCount(), lootEntryInfo.variationPercentage());
                    final ItemCountable newCountable = countable.copy();
                    newCountable.updateCount(rolledCount);
                    itemCountables.add(new ItemCountableLootEntryInfo(newCountable, lootEntryInfo));
                }
            }
        }

        return itemCountables;
    }
}
