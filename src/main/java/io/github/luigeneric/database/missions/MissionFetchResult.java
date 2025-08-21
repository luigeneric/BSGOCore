package io.github.luigeneric.database.missions;

/**
 * MissionInfoWrapper is a class that is to fetch a MissionCounterEntry out of the Mission
 * We do this using the serverId(to identify the mission inside the missionbook) and the counterGuid
 * the combination is unqueue for a player
 * playerId, missionId, counterGuid
 * @param serverId
 * @param missionGuid
 * @param counterCardGuid
 * @param currentCount
 * @param needCount
 */
public record MissionFetchResult(
        int serverId,
        long missionGuid,
        long associatedSectorCardGuid,
        long counterCardGuid,
        long currentCount,
        long needCount
)
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MissionFetchResult that = (MissionFetchResult) o;

        if (serverId != that.serverId) return false;
        return counterCardGuid == that.counterCardGuid;
    }

    @Override
    public int hashCode()
    {
        int result = serverId;
        result = 31 * result + (int) (counterCardGuid ^ (counterCardGuid >>> 32));
        return result;
    }
}
