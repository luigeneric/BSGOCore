package io.github.luigeneric.core.sector.management.lootsystem.claims;



import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.sector.management.damage.DamageRecord;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.List;
import java.util.Optional;

public abstract class LootClaim
{
    protected SpaceObject claimObject;
    protected final LootClaimType lootClaimType;

    protected LootClaim(final LootClaimType lootClaimType)
    {
        this.lootClaimType = lootClaimType;
    }

    public abstract void damageReceived(final DamageRecord damageRecord, final IParty party);

    public Optional<SpaceObject> getClaimObject()
    {
        return Optional.ofNullable(this.claimObject);
    }
    public List<SpaceObject> getClaimObjects()
    {
        if (claimObject == null)
            return List.of();
        return List.of(claimObject);
    }

    public LootClaimType getLootClaimType()
    {
        return lootClaimType;
    }
}
