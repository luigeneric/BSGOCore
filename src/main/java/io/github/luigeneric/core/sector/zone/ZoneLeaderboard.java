package io.github.luigeneric.core.sector.zone;

import io.github.luigeneric.core.protocols.zone.TournamentRankingData;

import java.util.*;

/**
 * Somehow save data of
 * - "leader" (I guess the player with most pts)
 * - Nemesis tracking
 *      nemesis if a killed you (b) n times
 * - killingspree
 */
public class ZoneLeaderboard
{
    private final Map<Long, Set<Long>> playerToNemesisKills;
    private final Map<Long, KillDeathScoreCnt> killDeathCntMap;

    ZoneLeaderboard(Map<Long, Set<Long>> playerToNemesisKills, Map<Long, KillDeathScoreCnt> killDeathCntMap)
    {
        this.playerToNemesisKills = playerToNemesisKills;
        this.killDeathCntMap = killDeathCntMap;
    }

    public List<TournamentRankingData> getTournamentRankingData()
    {
        final List<TournamentRankingData> tournamentRankingData = new ArrayList<>();
        for (Map.Entry<Long, KillDeathScoreCnt> longKillDeathScoreCntEntry : killDeathCntMap.entrySet())
        {
            tournamentRankingData.add(
                    new TournamentRankingData(
                            longKillDeathScoreCntEntry.getKey(),
                            0,
                            longKillDeathScoreCntEntry.getValue().getScore(),
                            longKillDeathScoreCntEntry.getValue().getKillCount(),
                            longKillDeathScoreCntEntry.getValue().getDeathCount())
            );
        }
        return tournamentRankingData;
    }

    public Optional<Long> getLeaderPlayerId()
    {
        return this.killDeathCntMap.entrySet()
                .stream()
                .max((o1, o2) -> Float.compare(o1.getValue().getScore(), o2.getValue().getScore()))
                .map(Map.Entry::getKey);
    }

    public void addNemesis(final long playerId, final long nemesisPlayerId)
    {
        final Set<Long> existingSet = playerToNemesisKills.getOrDefault(playerId, new HashSet<>());
        existingSet.add(nemesisPlayerId);
        playerToNemesisKills.put(playerId, existingSet);
    }

    public boolean isNemesisOfPlayer(final long playerId, final long suspectedNemesisId)
    {
        final Set<Long> nemesisSet = this.playerToNemesisKills.get(playerId);
        if (nemesisSet == null)
            return false;

        return nemesisSet.contains(suspectedNemesisId);
    }
}
