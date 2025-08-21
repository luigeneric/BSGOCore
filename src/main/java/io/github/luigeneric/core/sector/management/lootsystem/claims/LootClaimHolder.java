package io.github.luigeneric.core.sector.management.lootsystem.claims;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.sector.management.ObjectLeftSubscriber;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.sector.management.damage.AccumulatedDamage;
import io.github.luigeneric.core.sector.management.damage.DamageRecord;
import io.github.luigeneric.core.sector.management.damage.ObjectDamageHistory;
import io.github.luigeneric.core.sector.management.damage.SectorDamageHistory;
import io.github.luigeneric.core.sector.management.lootsystem.CounterCardDistributor;
import io.github.luigeneric.core.sector.management.lootsystem.LootDistributor;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class LootClaimHolder implements ObjectLeftSubscriber
{
    public static final int DEFAULT_PVP_CLAIM_DURATION_MS = 1000 * 20;
    public static final float MINIMUM_PERCENTAGE_DAMAGE = 0.1f;

    public static final int CLAIM_SECONDS_UNTIL_FREE_ASTEROIDS = 45;
    public static final int CLAIM_SECONDS_UNTIL_FREE_DEFAULT = 60;
    public static final float MINIMUM_DAMAGE_PERCENTAGE = 0.6f;

    private final SectorUsers users;
    private final Map<SpaceObject, LootClaim> lootClaimMap;
    private final LootDistributor lootDistributor;
    @Getter
    private final LootAssociations lootAssociations;
    private final SectorDamageHistory sectorDamageHistory;
    private final CounterCardDistributor counterCardDistributor;


    public LootClaimHolder(final SectorUsers users,
                           final LootAssociations lootAssociations,
                           final SectorDamageHistory sectorDamageHistory,
                           final LootDistributor lootDistributor,
                           final CounterCardDistributor counterCardDistributor
    )
    {
        this.users = users;
        this.lootClaimMap = new HashMap<>();
        this.lootAssociations = lootAssociations;
        this.sectorDamageHistory = sectorDamageHistory;
        this.lootDistributor = lootDistributor;
        this.counterCardDistributor = counterCardDistributor;
    }

    /**
     * Update claim and create a new one if not present
     * @param damageRecord target == Object of desire == to
     * @param user damage dealing user
     */
    public void updateClaim(final DamageRecord damageRecord, final User user)
    {
        final LootClaim lootClaim = lootClaimMap.getOrDefault(damageRecord.to(), getDefault(damageRecord));

        //doesn't matter I just need the party object whether it's null or not
        final IParty party = user.getPlayer().getParty().orElse(null);
        lootClaim.damageReceived(damageRecord, party);
        this.lootClaimMap.put(damageRecord.to(), lootClaim);
    }
    private LootClaim getDefault(final DamageRecord damageRecord)
    {
        //pvp claim
        if (damageRecord.to().isPlayer())
        {
            final ObjectDamageHistory dmgHistory = sectorDamageHistory.getDamageHistory(damageRecord.to());
            return new PvpClaim(dmgHistory, DEFAULT_PVP_CLAIM_DURATION_MS, MINIMUM_PERCENTAGE_DAMAGE);
        }
        //pve claim
        else
        {
            switch (damageRecord.to().getSpaceEntityType())
            {
                case Outpost ->
                {
                    return new OutpostClaim();
                }
                case Asteroid ->
                {
                    return new AsteroidClaim(CLAIM_SECONDS_UNTIL_FREE_ASTEROIDS);
                }
                default ->
                {
                    return new GeneralPveClaim(CLAIM_SECONDS_UNTIL_FREE_DEFAULT);
                }
            }
        }
    }

    public Optional<LootClaim> getClaim(final SpaceObject objectOfDesire)
    {
        return Optional.ofNullable(this.lootClaimMap.get(objectOfDesire));
    }
    public void removeClaim(final SpaceObject objectOfClaim)
    {
        if (objectOfClaim == null)
            return;
        this.lootClaimMap.remove(objectOfClaim);
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        final RemovingCause removingCause = arg.getRemovingCause();
        if (!removingCause.isOfType(RemovingCause.Death, RemovingCause.Collected))
        {
            removeClaim(arg.getRemovedSpaceObject());
            return;
        }
        final SpaceObject removedSpaceObject = arg.getRemovedSpaceObject();

        final Optional<LootClaim> optClaim = this.getClaim(removedSpaceObject);
        if (optClaim.isEmpty())
        {
            if (removedSpaceObject.isPlayer())
                log.info("ObjToRemove claim is null for player");
            return;
        }
        this.removeClaim(removedSpaceObject);

        final LootClaim claim = optClaim.get();
        final List<SpaceObject> claimObjects = claim.getClaimObjects();
        if (claimObjects.isEmpty())
            return;

        /*
        Right now we have 4 types and 3 different handles since PVE and ASTEROID_YIELD is the same as of now
         */
        switch (claim.lootClaimType)
        {
            case PVP ->
            {
                handlePvpClaim(claim, removedSpaceObject);
            }
            case OUTPOST ->
            {
                handleOutpostClaim(removedSpaceObject);
            }
            case PVE, ASTEROID_YIELD ->
            {
                handlePveAsteroidClaim(claimObjects, removedSpaceObject);
            }
        }
    }

    private void handlePveAsteroidClaim(List<SpaceObject> claimObjects, SpaceObject removedSpaceObject)
    {
        final SpaceObject claimObject = claimObjects.getFirst();

        final Optional<User> optClaimUser = this.users.getUser(claimObject.getPlayerId());
        if (optClaimUser.isEmpty())
            return;
        final User user = optClaimUser.get();

        //check if pve enmies did less than 60% of the damage
        boolean noLoot = isPveTooMuchDmgDone(removedSpaceObject);

        final Optional<Loot> optLoot = this.lootAssociations.getAndRemove(removedSpaceObject);

        if (noLoot)
        {
            return;
        }

        counterCardDistributor.pveKilled(user, removedSpaceObject, optLoot.orElse(null));
        if (optLoot.isEmpty())
        {
            return;
        }
        final Loot loot = optLoot.get();
        this.lootDistributor
                .pveLoot(user, removedSpaceObject, sectorDamageHistory.getDamageHistory(removedSpaceObject), loot);
    }

    private void handleOutpostClaim(SpaceObject removedSpaceObject)
    {
        final Optional<Loot> optLoot = lootAssociations.getAndRemove(removedSpaceObject);
        optLoot.ifPresent(loot ->
        {
            lootDistributor.outpostLoot(removedSpaceObject, loot, sectorDamageHistory.getDamageHistory(removedSpaceObject));
        });
        this.removeClaim(removedSpaceObject);
    }

    private void handlePvpClaim(LootClaim claim, SpaceObject removedSpaceObject)
    {
        if (!(claim instanceof PvpClaim pvpClaim))
        {
            return;
        }

        lootAssociations.getAndRemove(removedSpaceObject).ifPresent(loot ->
        {
            final PlayerShip killedPlayer = (PlayerShip) removedSpaceObject;
            lootDistributor.pvpLoot(pvpClaim, killedPlayer, loot);
        });

        this.removeClaim(removedSpaceObject);
    }

    /**
     * Prevent User from farming NPCs if NPC did main dmg
     * e.g. scenario: player above outpost(or near to it) --> player hits NPC --> outpost deals with it --> player farms loot
     * @implNote bro what the heck am Im doing here..
     * @param removedSpaceObject dead spaceObj
     * @return if NPC did too much dmg
     */
    private boolean isPveTooMuchDmgDone(final SpaceObject removedSpaceObject)
    {
        final ObjectDamageHistory dmgHistory = sectorDamageHistory.getDamageHistory(removedSpaceObject);
        final Optional<AccumulatedDamage> optHighestDealer = dmgHistory.getHighestDamageDealer();
        if (optHighestDealer.isEmpty())
        {
            return false;
        }

        final AccumulatedDamage highestDealer = optHighestDealer.get();
        final float highestSingleDmg = highestDealer.getAccumulatedDamage();
        if (!highestDealer.getDealer().isPlayer())
        {
            final float summedUp = dmgHistory.getSumDamage();
            final float dmgPercentageDone = highestSingleDmg / summedUp;
            return dmgPercentageDone > MINIMUM_DAMAGE_PERCENTAGE;
        }
        return false;
    }
}
