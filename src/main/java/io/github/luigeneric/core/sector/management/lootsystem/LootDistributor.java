package io.github.luigeneric.core.sector.management.lootsystem;

import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.ResourceCap;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.notification.NotificationProtocolWriteOnly;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorBlueprint;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.management.damage.ObjectDamageHistory;
import io.github.luigeneric.core.sector.management.lootsystem.claims.PvpClaim;
import io.github.luigeneric.core.sector.management.lootsystem.killtrace.PvpKillHistory;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootSource;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.loot.LootTemplate;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.utils.BgoRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class LootDistributor
{
    private final SectorUsers sectorUsers;
    private final BgoRandom bgoRandom;
    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;
    private final Tick tick;
    private final CounterCardDistributor counterCardDistributor;
    private final SectorBlueprint sectorBlueprint;
    private final MicrometerRegistry micrometerRegistry;
    private final GameServerParamsConfig gameServerParamsConfig;
    private final UsersContainer usersContainer;
    private final PvpKillHistory pvpKillHistory;

    private void lootToUser(final UserLoot userLoot, final SpaceObject killedObject)
    {
        final User user = userLoot.user();
        final long exp = userLoot.experience();
        final List<ItemCountableBonusType> items = userLoot.items();

        //only add exp if actually greater 0
        final Player player = user.getPlayer();
        if (exp > 0)
        {
            final PlayerProtocol playerProtocol = user.getProtocol(ProtocolID.Player);
            playerProtocol.addExperience(exp);
        }

        final List<ItemCountable> convertedCountablesToAdd = new ArrayList<>();
        //check for token cap
        for (final ItemCountableBonusType item : items)
        {
            //merit cap check
            final ResourceCap tokenCap = player.getMeritsCapFarmed();
            final boolean requiresUpdate = tokenCap
                    .increaseIfResource(item.itemCountable().getCardGuid(), (int) item.itemCountable().getCount());
            if (requiresUpdate)
            {
                final long delta = item.itemCountable().getCount() - tokenCap.getLastFarmedValue();
                //the old value was higher than the capped one --> convert to new resource
                if (delta > 0)
                {
                    convertedCountablesToAdd.add(ItemCountable.fromGUID(ResourceType.Uranium, delta));
                }
                item.itemCountable().updateCount(tokenCap.getLastFarmedValue());
                user.send(playerProtocolWriteOnly.writeMeritsCap(tokenCap));
            }
        }
        final List<ItemCountableBonusType> tmpConvertedItems = convertedCountablesToAdd
                .stream()
                .map(ItemCountableBonusType::new)
                .toList();
        items.addAll(tmpConvertedItems);


        //loot notification
        final NotificationProtocolWriteOnly notificationProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Notification);
        final BgoProtocolWriter bwNotification = notificationProtocolWriteOnly.writeLootMessage(items, userLoot.specialAction());
        user.send(bwNotification);
        counterCardDistributor.pvpKilled(user, killedObject, userLoot.specialAction());

        final LootSource lootSource = switch (killedObject.getSpaceEntityType())
        {
            case Player -> LootSource.PVP;
            case Asteroid -> LootSource.ASTEROID;
            default -> LootSource.PVE;
        };

        //add loot to inventory
        for (final ItemCountableBonusType item : items)
        {
            if (killedObject.getSpaceEntityType() == SpaceEntityType.Asteroid)
            {
                counterCardDistributor.asteroidResourceMined(user, item.itemCountable());
            }
            ContainerVisitor.addShipItem(user, item.itemCountable(), player.getHold());
            micrometerRegistry
                    .resourceEarned(
                            sectorBlueprint.sectorDesc().getSectorID(),
                            item.itemCountable().getCardGuid(),
                            user.getPlayer().getFaction(),
                            item.itemCountable().getCount(),
                            lootSource
                    );
        }
    }
    private void lootToUsers(final List<UserLoot> userLoots, final SpaceObject killedObject)
    {
        for (final UserLoot userLoot : userLoots)
        {
            lootToUser(userLoot, killedObject);
        }
    }

    /**
     * MiningShip loot!
     * @param user
     */
    public void oreMined(final User user, final ItemCountable itemCountable)
    {
        final Player player = user.getPlayer();

        //notification protocol
        final NotificationProtocolWriteOnly notificationProtocol = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Notification);
        final BgoProtocolWriter bwMinedOre = notificationProtocol.writeOreMined(itemCountable);
        counterCardDistributor.oreMined(user, itemCountable);
        player.getCounterFacade()
                .incrementCounter(
                        CounterCardType.mining_ships_income,
                        sectorBlueprint.sectorCards().sectorCard().getCardGuid(),
                        itemCountable.getCount()
                );
        user.send(bwMinedOre);


        micrometerRegistry
                .resourceEarned(
                        sectorBlueprint.sectorDesc().getSectorID(),
                        itemCountable.getCardGuid(),
                        player.getFaction(),
                        itemCountable.getCount(),
                        LootSource.PLANETOID_MINING
                );
        ContainerVisitor.addShipItem(user, itemCountable, player.getHold());
    }

    public void pveLoot(final User lootOwner,
                        final SpaceObject removedSpaceObject, final ObjectDamageHistory objectDamageHistory,
                        final Loot loot)
    {
        if (removedSpaceObject.isPlayer())
        {
            log.error("Removed pve SpaceObject was player!");
            return;
        }

        for (final LootTemplate lootTemplate : loot.getLootTemplateLst())
        {
            final List<ItemCountableLootEntryInfo> rolledItems = LootDistributorUtil
                    .rollItemsFromLootEntries(lootTemplate, bgoRandom);

            //get users of interest
            final Set<User> usersOfInterest = LootDistributorUtil
                    .getAssociatedLootUsers(lootTemplate, lootOwner, removedSpaceObject, objectDamageHistory, sectorUsers);


            //filter level and faction limitations
            List<UserItems> userItems = LootDistributorUtil
                    .filterForLevelFactions(usersOfInterest, rolledItems, lootTemplate.getExperience());

            if (removedSpaceObject.getSpaceEntityType() != SpaceEntityType.Outpost)
            {
                //update internal count
                userItems = LootDistributorUtil
                        .updateItemCountBasedOnGroupSize(userItems, usersOfInterest.size());
            }
            else
            {
                counterCardDistributor.outpostKilled(usersOfInterest);
            }

            final List<UserLoot> userLoots = LootDistributorUtil.applyFactorsUserItems(userItems);
            lootToUsers(userLoots, removedSpaceObject);
        }
    }

    /**
     * PVE Loot is distributed based on (probably outdated)
     *  1) most damage      100% loot
     *  2) kill-shot         70% loot
 *      3) assist damage     50% loot
     * @param pvpClaim x
     * @param objectDied  x
     * @param loot x
     */
    public void pvpLoot(final PvpClaim pvpClaim, final PlayerShip objectDied, final Loot loot)
    {
        for (LootTemplate lootTemplate : loot.getLootTemplateLst())
        {
            //roll all items
            final List<ItemCountableLootEntryInfo> rolledResult = LootDistributorUtil
                    .rollItemsFromLootEntries(lootTemplate, bgoRandom);

            //fetch results for pvp action
            final List<PvpResult> pvpResults = LootDistributorUtil
                    .determinePvpResults(
                            sectorUsers,
                            usersContainer,
                            pvpClaim, objectDied,
                            rolledResult,
                            lootTemplate.getExperience(),
                            tick, gameServerParamsConfig,
                            pvpKillHistory
                    );

            //filter for level and faction
            final List<UserItems> filteredPvpResults = LootDistributorUtil.filterForLevelFactions(pvpResults);

            //update items based on SpecialActionModifier
            final List<UserItems> updatedSizes = LootDistributorUtil
                    .updateItemCountBasedOnSpecialActionModifier(filteredPvpResults);

            //transform result into UserLoot
            final List<UserLoot> userLoots = LootDistributorUtil.applyFactorsUserItems(updatedSizes);

            //send UserLoot to each user
            lootToUsers(userLoots, objectDied);
        }
    }

    public void outpostLoot(final SpaceObject removedOp, final Loot loot,
                            final ObjectDamageHistory objectDamageHistory)
    {
        pveLoot(null, removedOp, objectDamageHistory, loot);
    }
}
