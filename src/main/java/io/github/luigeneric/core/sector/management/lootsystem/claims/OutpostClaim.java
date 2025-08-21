package io.github.luigeneric.core.sector.management.lootsystem.claims;


import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.sector.management.damage.DamageRecord;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.*;

public class OutpostClaim extends LootClaim
{
    private final Map<Long, SpaceObject> playerDamageDone;

    public OutpostClaim(final Map<Long, SpaceObject> playerDamageDone)
    {
        super(LootClaimType.OUTPOST);
        this.playerDamageDone = playerDamageDone;
    }

    public OutpostClaim()
    {
        this(new HashMap<>());
    }

    @Override
    public void damageReceived(final DamageRecord damageRecord, final IParty party)
    {
        if (!damageRecord.from().isPlayer())
            return;

        this.playerDamageDone.put(damageRecord.from().getPlayerId(), damageRecord.from());
    }

    @Override
    public List<SpaceObject> getClaimObjects()
    {
        return new ArrayList<>(this.playerDamageDone.values());
    }

    @Override
    public Optional<SpaceObject> getClaimObject()
    {
        throw new IllegalStateException("Claimobjects!!");
    }
}
