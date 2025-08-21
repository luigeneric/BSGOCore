package io.github.luigeneric.templates.missiontemplates;

import java.util.Set;

/**
 *
 * @param staticSectorId if 0, check ranom
 * @param useRandomSector if true ? -> check lists, false ? -> global
 * @param sectorIdsBlacklist //blacklist first
 * @param sectorIdsWhitelist
 */
public record MissionSectorDesc(long staticSectorId,
                                boolean useRandomSector,
                                Set<Long> sectorIdsBlacklist,
                                Set<Long> sectorIdsWhitelist
)
{
    public boolean isGlobal()
    {
        return staticSectorId == 0 && !useRandomSector;
    }
    public boolean isOnBlacklist(final long id)
    {
        return sectorIdsBlacklist.contains(id);
    }

    public boolean isOnWhitelist(final long id)
    {
        if (sectorIdsWhitelist.isEmpty())
            return true;
        return sectorIdsWhitelist.contains(id);
    }
}
