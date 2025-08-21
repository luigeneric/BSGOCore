package io.github.luigeneric.database;


import io.agroal.api.AgroalDataSource;
import io.github.luigeneric.core.player.Hangar;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.database.fetchresults.HangarInfoFetchResult;
import io.github.luigeneric.database.fetchresults.ShipInfoFetchResult;
import io.github.luigeneric.database.fetchresults.ShipSlotInfoFetchResult;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SqLiteHangar
{
    private final AgroalDataSource agroalDataSource;

    public HangarInfoFetchResult fetchHangar(final long playerID)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement hangarStatement = dbConnection.prepareStatement("SELECT * FROM hangars WHERE players_id=?");)
        {
            hangarStatement.setLong(1, playerID);
            try(final ResultSet resultSet = hangarStatement.executeQuery();)
            {
                if (!resultSet.next())
                {
                    throw new IllegalStateException("Fetching player but no active ship index!");
                }
                final int activeIndex = resultSet.getInt("active_index");
                final List<ShipInfoFetchResult> playerHangarShipsInfos = fetchPlayerHangarShipInfos(playerID);
                return new HangarInfoFetchResult(activeIndex, playerHangarShipsInfos);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private List<ShipSlotInfoFetchResult> fetchShipSlot(final long playerID, final int shipID)
    {
        final List<ShipSlotInfoFetchResult> shipSlotInfoFetchResults = new ArrayList<>();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement shipsStatement = dbConnection.prepareStatement("SELECT * FROM shipSlots " +
                "WHERE players_id=? AND ship_id=?");)
        {
            shipsStatement.setLong(1, playerID);
            shipsStatement.setInt(2, shipID);
            try(final ResultSet resultSet = shipsStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    final int serverID = resultSet.getInt("server_id");
                    final long guid = resultSet.getLong("guid");
                    final float durability = resultSet.getFloat("durability");

                    shipSlotInfoFetchResults.add(new ShipSlotInfoFetchResult(serverID, guid, durability));
                }
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }


        return shipSlotInfoFetchResults;
    }

    private List<ShipInfoFetchResult> fetchPlayerHangarShipInfos(final long playerID)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement shipsStatement = dbConnection.prepareStatement("SELECT * FROM players_hangar_ships WHERE players_id=?");)
        {
            shipsStatement.setLong(1, playerID);
            try(final ResultSet shipsResultSet = shipsStatement.executeQuery())
            {
                final List<ShipInfoFetchResult> shipInfoFetchResults = new ArrayList<>();

                while(shipsResultSet.next())
                {
                    final int serverID = shipsResultSet.getInt("server_id");
                    final long guid = shipsResultSet.getLong("guid");
                    final float durability = shipsResultSet.getFloat("durability");
                    final String name = shipsResultSet.getString("name");

                    //fetch the slots of the ship
                    final List<ShipSlotInfoFetchResult> slotsFetched = fetchShipSlot(playerID, serverID);
                    log.info("player " + playerID + " fetch HangarShip " + guid + " " + serverID);

                    shipInfoFetchResults.add(new ShipInfoFetchResult(guid, serverID, durability, name, slotsFetched));
                }
                return shipInfoFetchResults;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }


    @Transactional
    public void writeHangar(final Player player)
    {
        final Hangar hangar = player.getHangar();
        try (var dbConnection = agroalDataSource.getConnection();
             final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO hangars(players_id, active_index)" +
                     " VALUES (?, ?)");)
        {
            ps.setLong(1, player.getUserID());
            ps.setInt(2, hangar.getActiveShip().getServerId());
            ps.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try (var dbConnection = agroalDataSource.getConnection();
             final PreparedStatement deletePs = dbConnection.prepareStatement("DELETE FROM players_hangar_ships WHERE players_id=?");
             final PreparedStatement insertPs = dbConnection.prepareStatement("REPLACE INTO players_hangar_ships(" +
                     "players_id, server_id, guid, durability, name)" +
                     " VALUES (?, ?, ?, ?, ?)"))
        {
            deletePs.setLong(1, player.getUserID());
            deletePs.executeUpdate();

            for (final HangarShip ship : hangar.getAllHangarShips())
            {
                log.info("Write HangarShip of user " + player.getUserID() + " to db " + ship.getCardGuid() + " " + ship.getServerId());

                insertPs.setLong(1, player.getUserID());
                insertPs.setInt(2, ship.getServerId());
                insertPs.setLong(3, ship.getCardGuid());
                insertPs.setFloat(4, ship.getDurability());
                insertPs.setString(5, ship.getName());

                insertPs.addBatch();
            }
            insertPs.executeBatch();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try (var dbConnection = agroalDataSource.getConnection();
             final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO shipSlots(" +
                     "players_id, ship_id, server_id, guid, durability)" +
                     " VALUES (?, ?, ?, ?, ?)");)
        {
            for (final HangarShip ship : hangar.getAllHangarShips())
            {
                final ShipSlots slots = ship.getShipSlots();
                for (final ShipSlot slot : slots.values())
                {
                    final ShipSystem system = slot.getShipSystem();
                    if (system == null)
                        continue;
                    ps.setLong(1, player.getUserID());
                    ps.setInt(2, ship.getServerId());
                    ps.setInt(3, system.getServerID());
                    ps.setLong(4, system.getCardGuid());
                    ps.setFloat(5, system.getDurability());
                    ps.addBatch();
                }
                ps.executeBatch(); //execute on every ship
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}
