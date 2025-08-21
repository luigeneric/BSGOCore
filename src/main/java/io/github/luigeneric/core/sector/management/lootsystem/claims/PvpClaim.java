package io.github.luigeneric.core.sector.management.lootsystem.claims;

import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.damage.AccumulatedDamage;
import io.github.luigeneric.core.sector.management.damage.DamageRecord;
import io.github.luigeneric.core.sector.management.damage.ObjectDamageHistory;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class PvpClaim extends LootClaim
{
    private final long claimTimeUntilFree;
    private final ObjectDamageHistory objectDamageHistory;
    private AccumulatedDamage killShotObject;
    private final float minimumPercentageDamage;

    public PvpClaim(final ObjectDamageHistory objectDamageHistory, final long claimTimeClaimValid, final float minimumPercentageDamage)
    {
        super(LootClaimType.PVP);
        this.objectDamageHistory = objectDamageHistory;
        this.claimTimeUntilFree = claimTimeClaimValid;
        this.minimumPercentageDamage = minimumPercentageDamage;
    }

    @Override
    public void damageReceived(final DamageRecord damageRecord, final IParty party)
    {
        //ignore non player dmg
        final Optional<AccumulatedDamage> optHighest = objectDamageHistory.getHighestDamageDealer();
        optHighest.ifPresent(accumulatedDamage ->
        {
            this.claimObject = accumulatedDamage.getDealer();
        });

        if (damageRecord.isKillShot())
        {
            final Optional<AccumulatedDamage> opt = objectDamageHistory.getByObjectID(damageRecord.from().getObjectID());
            opt.ifPresent(ac -> this.killShotObject = ac);
        }
    }

    public Optional<AccumulatedDamage> getKillShotObject()
    {
        return Optional.ofNullable(this.killShotObject);
    }

    public Set<AccumulatedDamage> getSortedByDmg()
    {
        return objectDamageHistory.getSortedByDamage();
    }
    public List<AccumulatedDamage> getAllDmgDealer(final Predicate<AccumulatedDamage> predicate)
    {
        return this.objectDamageHistory.getAll(predicate);
    }

    public float getMinimumPercentageDamage()
    {
        return minimumPercentageDamage;
    }

    public long getClaimTimeUntilFree()
    {
        return claimTimeUntilFree;
    }

    public Set<AccumulatedDamage> getAllAssistedParticipants(final float maxHp, final Tick tick)
    {
        final Set<AccumulatedDamage> resultSet = new HashSet<>();
        final Set<AccumulatedDamage> sortedByDmg = this.getSortedByDmg();
        final Optional<SpaceObject> claimObj = getClaimObject();

        for (AccumulatedDamage accumulatedDamage : sortedByDmg)
        {
            final SpaceObject assistingDealer = accumulatedDamage.getDealer();

            if (claimObj.isPresent() && assistingDealer.equals(claimObj.get()))
            {
                continue;
            }

            final var optKillShot = this.getKillShotObject();
            if (optKillShot.isPresent())
            {
                final SpaceObject killShotdealer = optKillShot.get().getDealer();
                if (killShotdealer.equals(assistingDealer))
                    continue;
            }
            final boolean damageHighEnough = (maxHp * getMinimumPercentageDamage()) < accumulatedDamage.getAccumulatedDamage();
            if (damageHighEnough)
            {
                final boolean timeStampIsInvalid = (accumulatedDamage.getLastTime() + getClaimTimeUntilFree()) < tick.getTimeStamp();
                if (timeStampIsInvalid)
                {
                    continue;
                }
                resultSet.add(accumulatedDamage);
            }
        }
        return resultSet;
    }
}
