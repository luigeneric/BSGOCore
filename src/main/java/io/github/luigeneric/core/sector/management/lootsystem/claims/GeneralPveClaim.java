package io.github.luigeneric.core.sector.management.lootsystem.claims;

public class GeneralPveClaim extends PveClaim
{
    public GeneralPveClaim(final long claimSecondsUntilFree)
    {
        super(LootClaimType.PVE, claimSecondsUntilFree);
    }
}
