package io.github.luigeneric.core.sector.management.lootsystem.claims;


import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.sector.management.damage.DamageRecord;

public abstract class PveClaim extends LootClaim
{
    protected final ClaimTimeStamp claimTimeStamp;

    public PveClaim(final LootClaimType lootClaimType, final long claimSecondsUntilFree)
    {
        super(lootClaimType);
        this.claimTimeStamp = new ClaimTimeStamp(claimSecondsUntilFree * 1000);
    }

    @Override
    public void damageReceived(final DamageRecord damageRecord, final IParty party)
    {
        this.updateClaimTimeStamp(damageRecord, party);
    }

    protected void updateClaimTimeStamp(final DamageRecord damageRecord, final IParty party)
    {
        final ClaimUpdateResult claimUpdateResult = claimTimeStamp.update(damageRecord.timeStamp(), damageRecord.from(), party);
        if (claimUpdateResult.isUpdated())
        {
            if (!claimUpdateResult.isFromParty())
            {
                this.claimObject = claimTimeStamp.getDmgDoneBy();
            }
        }
    }
}
