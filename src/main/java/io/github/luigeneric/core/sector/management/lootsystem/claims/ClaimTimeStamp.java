package io.github.luigeneric.core.sector.management.lootsystem.claims;


import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.spaceentities.SpaceObject;

public class ClaimTimeStamp
{
    private final long timeUntilFree;
    private long lastClaimTime;
    private boolean isFirst;
    private SpaceObject dmgDoneBy;

    public ClaimTimeStamp(final long timeUntilFree)
    {
        this.timeUntilFree = timeUntilFree;
        this.lastClaimTime = 0;
        this.isFirst = true;
    }

    /**
     * Updates the current timestamp
     * @param current current timestamp of the DamageRecord
     * @param dmgDoneBy current object of the DamageRecord
     * @param party party of the dmg dealer
     * @return true if and only if the claim got an update
     */
    public ClaimUpdateResult update(final long current, final SpaceObject dmgDoneBy, final IParty party)
    {
        //no damage done yet
        if (isFirst)
        {
            isFirst = false;
            this.dmgDoneBy = dmgDoneBy;
            lastClaimTime = current;
            return new ClaimUpdateResult(false, true);
        }

        //there is already an existing claim
        // dmg done by object or damage done by party member
        final boolean isInParty = party != null && party.isInParty(this.dmgDoneBy.getPlayerId());
        if (this.dmgDoneBy.equals(dmgDoneBy) || isInParty)
        {
            this.lastClaimTime = current;
            //just keep it as it was for now
            this.dmgDoneBy = dmgDoneBy; //Still overwrite because it's from the party
            return new ClaimUpdateResult(isInParty, true);
        }
        //it's not the same object nor from the same party
        else
        {
            //time is up!
            if ((lastClaimTime + timeUntilFree) < current)
            {
                lastClaimTime = current;
                this.dmgDoneBy = dmgDoneBy;
                return new ClaimUpdateResult(false, true);
            }
        }

        return new ClaimUpdateResult(false, false);
    }

    public long getTimeUntilFree()
    {
        return timeUntilFree;
    }

    public SpaceObject getDmgDoneBy()
    {
        return dmgDoneBy;
    }
}
