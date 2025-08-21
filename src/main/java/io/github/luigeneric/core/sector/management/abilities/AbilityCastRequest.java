package io.github.luigeneric.core.sector.management.abilities;


import io.github.luigeneric.core.spaceentities.Ship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AbilityCastRequest
{
    private final Ship castingShip;
    private final int abilityID;
    private final long[] targetIDs;

    private final boolean isAutoCastAbility;

    public AbilityCastRequest(final Ship castingShip, final int abilityID, final boolean isAutoCastAbility,
                              final long... targetIDs) throws IllegalArgumentException
    {
        Objects.requireNonNull(castingShip, "CastingShip was null!");
        Objects.requireNonNull(targetIDs, "TargetIDs was null!");

        if (containsDuplicateEntry(targetIDs))
        {
            throw new IllegalArgumentException("TargetID-List contained double entries!");
        }
        this.castingShip = castingShip;
        this.abilityID = abilityID;
        this.targetIDs = targetIDs;
        this.isAutoCastAbility = isAutoCastAbility;
    }

    public AbilityCastRequest(final Ship castingShip, final int abilityID, final boolean isAutoCastAbility,
                              final Set<Long> targetIDs) throws IllegalArgumentException
    {
        Objects.requireNonNull(castingShip, "CastingShip was null!");
        Objects.requireNonNull(targetIDs, "TargetIDs was null!");

        this.castingShip = castingShip;
        this.abilityID = abilityID;
        final long[] tmp = new long[targetIDs.size()];
        int c = 0;
        for (final long targetID : targetIDs)
        {
            tmp[c++] = targetID;
        }
        this.targetIDs = tmp;
        this.isAutoCastAbility = isAutoCastAbility;
    }


    private boolean containsDuplicateEntry(final long[] targetIDs)
    {
        if (targetIDs.length == 1)
            return false;

        final HashSet<Long> set = new HashSet<>(targetIDs.length);
        for (final long targetID : targetIDs)
        {
            final boolean didNotContain = set.add(targetID);
            if (!didNotContain)
            {
                return true;
            }
        }
        return false;
    }

    public int getAbilityID()
    {
        return abilityID;
    }

    public long[] getTargetIDs()
    {
        return targetIDs;
    }


    public boolean isAutoCastAbility()
    {
        return isAutoCastAbility;
    }

    public Ship getCastingShip()
    {
        return castingShip;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbilityCastRequest that = (AbilityCastRequest) o;

        if (abilityID != that.abilityID) return false;
        return castingShip.equals(that.castingShip);
    }

    @Override
    public int hashCode()
    {
        int result = castingShip.hashCode();
        result = 31 * result + abilityID;
        return result;
    }

    public long generateKey()
    {
        return generateKey(this.castingShip.getObjectID(), this.abilityID);
    }
    public static long generateKey(final long objectID, final int abilityID)
    {
        long num = abilityID;
        num <<= 32;
        return num + objectID;
    }
}
