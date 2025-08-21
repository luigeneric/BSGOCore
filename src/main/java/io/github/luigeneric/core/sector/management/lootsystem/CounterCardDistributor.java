package io.github.luigeneric.core.sector.management.lootsystem;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.counters.CounterFacade;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.management.damage.AccumulatedDamage;
import io.github.luigeneric.core.sector.management.lootsystem.claims.PvpClaim;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.WeaponPlatform;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.enums.SpecialAction;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.cards.SectorCard;
import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CounterCardDistributor
{
    private final SectorUsers users;
    private final SectorCard sectorCard;

    public CounterCardDistributor(SectorUsers users, SectorCard sectorCard)
    {
        this.users = users;
        this.sectorCard = sectorCard;
    }

    public void outpostKilled(final Set<User> usersOfInterest)
    {
        for (final User user : usersOfInterest)
        {
            incrementCounter(user, CounterCardType.outposts_killed);
        }
    }
    /**
     *
     * @param user user object
     * @param killedSpaceObject may be ship or not, but never player
     * @param loot loot object, may be null
     */
    public void pveKilled(final User user, final SpaceObject killedSpaceObject, final Loot loot)
    {
        final boolean hasLoot = loot != null;

        if (Faction.invert(killedSpaceObject.getFaction()) == user.getPlayer().getFaction() && killedSpaceObject.isShip())
        {
            incrementCounter(user, CounterCardType.opposite_faction_killed);
        }

        switch (killedSpaceObject.getSpaceEntityType())
        {
            case MiningShip ->
            {
                incrementCounter(user, CounterCardType.mining_ships_killed);
            }
            case Comet ->
            {
                incrementCounter(user, CounterCardType.comets_killed);
            }
        }
        if (killedSpaceObject instanceof Ship killedShip)
        {
            overallShipKilled(user, killedShip);
        }
    }

    private void overallShipKilled(final User user, final Ship killedShip)
    {
        incrementCounter(user, CounterCardType.pve_killed);

        if (killedShip.getFaction() == Faction.Ancient)
        {
            overallAncientKilled(user, killedShip);
        }
        else
        {
            colonialShipObjectKeyFilter(user, killedShip);
        }
    }

    private void colonialShipObjectKeyFilter(final User user, final Ship killedShip)
    {
        final long shipObjectKey = killedShip.getShipCard().getShipObjectKey();
        //mk2
        if (shipObjectKey == 107780547L)
        {
            incrementCounter(user, CounterCardType.vipers_killed);
        }
        //raptor
        else if (shipObjectKey == 116493059L)
        {
            incrementCounter(user, CounterCardType.raptors_killed);
        }
        //rhino
        else if (shipObjectKey == 107966800L)
        {
            incrementCounter(user, CounterCardType.rhinos_killed);
        }
        //mk7
        else if (shipObjectKey == 163729272L)
        {
            incrementCounter(user, CounterCardType.viper_mk7_killed);
        }
        //mk3
        else if (shipObjectKey == 163729268L)
        {
            incrementCounter(user, CounterCardType.viper_mk3s_killed);
        }
    }

    public void overallAncientKilled(final User user, final Ship ship)
    {
        incrementCounter(user, CounterCardType.ancients_killed);
        if (ship instanceof WeaponPlatform weaponPlatform)
        {
            overallAncientStationaryKilled(user);
        } else if (ship.getShipCard().getShipObjectKey() == 2 ||
        ship.getShipCard().getShipObjectKey() == 40 ||
                ship.getShipCard().getShipObjectKey() == 41)
        {
            incrementCounter(user, CounterCardType.drones_killed);
        }
    }
    private void overallAncientStationaryKilled(final User user)
    {
        incrementCounter(user, CounterCardType.stationaries_killed);
    }

    public void pvpKilled(final User user, final SpaceObject killedSpaceObject, final List<SpecialAction> specialActions)
    {
        for (final SpecialAction specialAction : specialActions)
        {
            switch (specialAction)
            {
                case Killer, AssistCountingAsKill ->
                {
                    incrementCounter(user, CounterCardType.pvp_killed);
                    incrementCounter(user, CounterCardType.pvp_action_killer);

                    if (Faction.invert(killedSpaceObject.getFaction()) == user.getPlayer().getFaction())
                    {
                        incrementCounter(user, CounterCardType.opposite_faction_killed);
                    }
                }
                case Assist -> incrementCounter(user, CounterCardType.pvp_action_assist);
                case Buffer -> incrementCounter(user, CounterCardType.pvp_action_buffer);
                case Debuffer -> incrementCounter(user, CounterCardType.pvp_action_debuffer);
            }
        }
    }
    @Deprecated
    public void pvpKilled(final PvpClaim pvpClaim, final PlayerShip killedSpaceObject)
    {
        final Optional<SpaceObject> mostDmg = pvpClaim.getClaimObject();
        final Optional<AccumulatedDamage> killer = pvpClaim.getKillShotObject();
        //final Set<AccumulatedDamage> sortedByDmg = pvpClaim.getSortedByDmg();

        mostDmg.ifPresent(spaceObject ->
        {
            if (!spaceObject.isPlayer())
                return;

            final Optional<User> optUsr = users.getUser(spaceObject.getPlayerId());
            if (optUsr.isEmpty())
                return;
            final User user = optUsr.get();
            incrementCounter(user, CounterCardType.pvp_killed);
            incrementCounter(user, CounterCardType.pvp_action_killer);

            if (Faction.invert(killedSpaceObject.getFaction()) == user.getPlayer().getFaction())
            {
                incrementCounter(user, CounterCardType.opposite_faction_killed);
            }
        });

        killer.ifPresent(killerDmg ->
        {
            if (!killerDmg.getDealer().isPlayer())
                return;
            final AtomicBoolean isEquals = new AtomicBoolean(false);
            mostDmg.ifPresent(mostDmgBy ->
            {
                if (mostDmgBy.equals(killerDmg.getDealer()))
                    isEquals.set(true);
            });
            if (isEquals.get())
                return;

            final var optUser = users.getUser(killerDmg.getDealer().getPlayerId());
            if (optUser.isEmpty())
                return;
            User usr = optUser.get();

            incrementCounter(usr, CounterCardType.pvp_killed);
            incrementCounter(usr, CounterCardType.pvp_action_killer);
            if (Faction.invert(killedSpaceObject.getFaction()) == usr.getPlayer().getFaction())
            {
                incrementCounter(usr, CounterCardType.opposite_faction_killed);
            }
        });
    }

    public void oreMined(final User user, final ItemCountable itemCountable)
    {
        asteroidResourceMined(user, itemCountable);
    }
    public void asteroidResourceMined(final User user, final ItemCountable itemCountable)
    {
        final ResourceType resourceType = ResourceType.forValue(itemCountable.getCardGuid());
        if (resourceType == null)
            return;
        if (resourceType == ResourceType.None)
            return;

        incrementCounter(user, CounterCardType.asteroids_mined);
        CounterCardType counterCardType = null;
        switch (resourceType)
        {
            case Water ->
            {
                counterCardType = CounterCardType.water_mined;
            }
            case Tylium ->
            {
                counterCardType = CounterCardType.tylium_mined;
            }
            case Titanium ->
            {
                counterCardType = CounterCardType.titanium_mined;
            }
        }
        if (counterCardType == null)
            return;

        incrementCounter(user, counterCardType.cardGuid, itemCountable.getCount());
    }

    private void incrementCounter(final User user, final CounterCardType counterCardType)
    {
        this.incrementCounter(user, counterCardType.cardGuid);
    }
    private void incrementCounter(final User user, final long counterCardGuid)
    {
        this.incrementCounter(user, counterCardGuid, 1);
    }
    private void incrementCounter(final User user, final long counterCardGuid, final double byValue)
    {
        final CounterFacade counterFacade = user.getPlayer().getCounterFacade();
        counterFacade.incrementCounter(counterCardGuid, this.sectorCard.getCardGuid(), byValue);
    }
}
