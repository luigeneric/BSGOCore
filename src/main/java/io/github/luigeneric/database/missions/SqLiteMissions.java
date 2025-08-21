package io.github.luigeneric.database.missions;


import io.agroal.api.AgroalDataSource;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.counters.Mission;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.counters.MissionCountable;
import io.github.luigeneric.database.SqLiteProvider;
import io.github.luigeneric.templates.cards.Card;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.SectorCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Identify a mission based on
 * playerId, missionId, counterGuid
 * the combination of these 3 is unique
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SqLiteMissions
{
    private final AgroalDataSource agroalDataSource;
    private final Catalogue catalogue;

    public void writeMissionsToDb(final Player player)
    {
        writeMissionBook(player);
        writeMissions(player);
    }
    private void writeMissionBook(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
                final PreparedStatement ps = dbConnection.prepareStatement(
                "REPLACE INTO mission_books(player_id, last_time_missions_fetch_date)" +
                        " VALUES (?, ?)"))
        {
            ps.setLong(1, player.getUserID());
            final LocalDateTime lastTimeMissionsRequested = player.getCounterFacade().missionBook().getLastTimeMissionsRequested();
            final String lastTimeMissionsRequestedStr = lastTimeMissionsRequested == null ? "" : lastTimeMissionsRequested.toString();
            ps.setString(2, lastTimeMissionsRequestedStr);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void writeMissions(final Player player)
    {
        final MissionBook missionBook = player.getCounterFacade().missionBook();

        try
        {
            try (var dbConnection = agroalDataSource.getConnection();
                 PreparedStatement deletePs = dbConnection.prepareStatement("DELETE FROM player_missions WHERE player_id=?"))
            {
                deletePs.setLong(1, player.getUserID());
                deletePs.executeUpdate();
            }


            try (var dbConnection = agroalDataSource.getConnection();
                 final PreparedStatement ps = dbConnection.prepareStatement(
                    "REPLACE INTO player_missions(player_id, mission_id, mission_guid, associated_sector_guid, counter_guid, current_count, need_count)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?)");)
            {
                final List<Mission> missions = missionBook.findAll(mission -> true);
                for (final Mission mission : missions)
                {
                    for (final MissionCountable missionCountable : mission.getMissionCountables().values())
                    {
                        ps.setLong(1, player.getUserID());
                        ps.setInt(2, mission.getServerID());
                        ps.setLong(3, mission.getMissionCardGUID());
                        ps.setLong(4, mission.getAssociatedSectorCardGUID());
                        ps.setLong(5, missionCountable.getCounterCardGuid());
                        ps.setLong(6, missionCountable.getCurrentCount());
                        ps.setLong(7, missionCountable.getNeedCount());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }
    public void fetchMissionBook(final Player player)
    {
        final MissionBook missionBook = player.getCounterFacade().missionBook();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT * FROM mission_books WHERE player_id=?");)
        {
            preparedStatement.setLong(1, player.getUserID());

            try(final ResultSet queryResult = preparedStatement.executeQuery();)
            {
                if (!queryResult.next())
                    return;

                final String rawLastTimeMissionFetched = queryResult.getString("last_time_missions_fetch_date");
                final LocalDateTime lastTimeMissionFetched = SqLiteProvider.parseDateTimeFromString(rawLastTimeMissionFetched);
                missionBook.setLastTimeMissionsRequested(lastTimeMissionFetched);

                final List<MissionFetchResult> missionFetchResults = fetchMissionInfo(player.getUserID());
                Map<Integer, List<MissionFetchResult>> resultGrouped = missionFetchResults.stream()
                        .collect(Collectors.groupingBy(MissionFetchResult::serverId));

                for (Map.Entry<Integer, List<MissionFetchResult>> integerListEntry : resultGrouped.entrySet())
                {
                    final List<MissionFetchResult> entriesForMission = integerListEntry.getValue();
                    final Map<Long, MissionCountable> missionCountables = new HashMap<>();
                    final MissionFetchResult firstEntry = integerListEntry.getValue().get(0);
                    final Optional<Card> optMissionCard = catalogue.fetchCard(firstEntry.missionGuid(), CardView.Mission);
                    if (optMissionCard.isEmpty())
                    {
                        log.warn("Db, tried to fetch mission guid {} but was null!", firstEntry.missionGuid());
                        continue;
                    }
                    for (MissionFetchResult missionFetchResult : entriesForMission)
                    {
                        missionCountables
                                .put(missionFetchResult.counterCardGuid(),
                                        new MissionCountable(
                                                missionFetchResult.counterCardGuid(),
                                                missionFetchResult.currentCount(),
                                                missionFetchResult.needCount()
                                        ));

                    }

                    Mission mission = new Mission(
                            firstEntry.serverId(),
                            firstEntry.missionGuid(),
                            firstEntry.associatedSectorCardGuid(),
                            missionCountables
                    );
                    missionBook.inject(mission);
                }
            }
        }
        catch (SQLException e)
        {
            log.error("SQL Error inside fetchMissionBook", e);
        }
    }
    private List<MissionFetchResult> fetchMissionInfo(final long playerId) throws SQLException
    {
        final List<MissionFetchResult> missionFetchResults = new ArrayList<>();

        try(var dbConnection = agroalDataSource.getConnection();
                final PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT * FROM player_missions WHERE player_id=?"))
        {
            preparedStatement.setLong(1, playerId);

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    final int missionId = resultSet.getInt("mission_id");
                    final long missionGuid = resultSet.getLong("mission_guid");
                    final long associatedSectorGuid = resultSet.getLong("associated_sector_guid");
                    final long cleanedAssociatedSectorGuid = cleanMissionSectorGuid(associatedSectorGuid);
                    final long counterGuid = resultSet.getLong("counter_guid");
                    final long currentCount = resultSet.getLong("current_count");
                    final long needCount = resultSet.getLong("need_count");
                    final MissionFetchResult missionFetchResult = new MissionFetchResult(
                            missionId,
                            missionGuid,
                            cleanedAssociatedSectorGuid,
                            counterGuid,
                            currentCount,
                            needCount
                    );
                    missionFetchResults.add(missionFetchResult);
                }
            }
        }

        return missionFetchResults;
    }

    private long cleanMissionSectorGuid(final long associatedSectorGuid)
    {
        //all sectors stays all sectors
        if (associatedSectorGuid == 0)
            return 0;

        //fetch sector card
        final Optional<SectorCard> optSectorGuid = catalogue.fetchCard(associatedSectorGuid, CardView.Sector);

        //if sector card is not present, there is no sector with the guid, map to all systems
        if (optSectorGuid.isEmpty())
            return 0;

        //sector card is present so the guid is valid
        return associatedSectorGuid;
    }
}

