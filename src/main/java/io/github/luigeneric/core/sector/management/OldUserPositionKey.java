package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.enums.Faction;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * userId, faction and hold timestamp but without hash
 * @param userID
 * @param faction
 */
public record OldUserPositionKey(long userID, Faction faction, LocalDateTime localDateTime)
{
    public OldUserPositionKey(long userID, Faction faction)
    {
        this(userID, faction, LocalDateTime.now(Clock.systemUTC()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(userID, faction);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        OldUserPositionKey other = (OldUserPositionKey) obj;
        return userID == other.userID && faction == other.faction;
    }
}
