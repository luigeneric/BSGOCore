package io.github.luigeneric.core.sector.management.lootsystem.claims;

public class AsteroidClaim extends PveClaim
{
    protected AsteroidClaim(final long claimSecondsUntilFree)
    {
        super(LootClaimType.ASTEROID_YIELD, claimSecondsUntilFree);
    }
}
