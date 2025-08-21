package io.github.luigeneric.database;

import io.agroal.api.AgroalDataSource;
import io.github.luigeneric.core.player.MailBox;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainer;
import io.github.luigeneric.core.player.container.Mail;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SqlLiteContainers
{
    private final AgroalDataSource agroalDataSource;
    private final GameServerParamsConfig gameServerParamsConfig;



    public void deleteMailsFromContainers(final Player player)
    {
        this.deleteItemsFromContainer(player, ContainerType.Mail);

        deleteMails(player);
    }

    private void deleteMails(final Player player)
    {
        final MailBox mailBox = player.getMailBox();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement deleteMails = dbConnection.prepareStatement("DELETE FROM mails WHERE players_id = ? AND " +
                "mail_id = ?");)
        {
            for (final Mail mail : mailBox.getItems().values())
            {
                deleteMails.setLong(1, player.getUserID());
                deleteMails.setInt(2, mail.getServerID());
                deleteMails.addBatch();
            }
            deleteMails.executeBatch();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void deleteItemsFromContainer(final Player player, final ContainerType type)
    {

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement deleteContainers = dbConnection.prepareStatement("DELETE FROM containers WHERE players_id = ? AND " +
                "container_types_id = ?");)
        {
            deleteContainers.setLong(1, player.getUserID());
            deleteContainers.setInt(2, type.value);

            deleteContainers.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement deleteShipSystems = dbConnection.prepareStatement("DELETE FROM ship_systems WHERE players_id = ? AND " +
                "containers_id = ?");)
        {
            deleteShipSystems.setLong(1, player.getUserID());
            deleteShipSystems.setLong(2, type.value);
            deleteShipSystems.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement deleteItemCountables = dbConnection.prepareStatement("DELETE FROM item_countables WHERE players_id = ? " +
                "AND containers_id = ?");)
        {
            deleteItemCountables.setLong(1, player.getUserID());
            deleteItemCountables.setLong(2, type.value);
            deleteItemCountables.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void writeItemsToContainer(final Player player, final IContainer container)
    {
        final ContainerType type = container.getContainerID().getContainerType();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement shipSystems =
                    dbConnection.prepareStatement("INSERT INTO ship_systems(" +
                            "players_id, containers_id, server_id, guid, durability)" +
                            "VALUES (?, ?, ?, ?, ?)");)
        {
            for (final int itemId : container.getAllItemsIDs())
            {
                final ShipItem item = container.getByID(itemId);
                if (item instanceof ShipSystem system)
                {
                    shipSystems.setLong(1, player.getUserID());
                    shipSystems.setInt(2, type.value);
                    shipSystems.setInt(3, system.getServerID());
                    shipSystems.setLong(4, system.getCardGuid());
                    shipSystems.setFloat(5, system.getDurability());
                    shipSystems.addBatch();
                }
            }
            shipSystems.executeBatch();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement shipCountables =
                    dbConnection.prepareStatement("INSERT INTO item_countables(" +
                            "players_id, containers_id, server_id, guid, count)" +
                            "VALUES (?, ?, ?, ?, ?)");)
        {
            for (final int itemId : container.getAllItemsIDs())
            {
                final ShipItem item = container.getByID(itemId);
                if (item instanceof ItemCountable countable)
                {
                    shipCountables.setLong(1, player.getUserID());
                    shipCountables.setInt(2, type.value);
                    shipCountables.setInt(3, countable.getServerID());
                    shipCountables.setLong(4, countable.getCardGuid());
                    shipCountables.setFloat(5, countable.getCount());
                    shipCountables.addBatch();
                }
            }
            shipCountables.executeBatch();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    public void writeContainers(final Player player)
    {
        this.deleteItemsFromContainer(player, player.getHold().getContainerID().getContainerType());
        this.deleteItemsFromContainer(player, player.getLocker().getContainerID().getContainerType());

        //prepare overall containers
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement addContainers = dbConnection.prepareStatement(
                "INSERT INTO containers(container_types_id, players_id)" +
                        "VALUES (?, ?)");)
        {
            //add all

            addContainers.setInt(1, player.getHold().getContainerID().getContainerType().value);
            addContainers.setLong(2, player.getUserID());
            addContainers.addBatch();

            addContainers.setInt(1, player.getLocker().getContainerID().getContainerType().value);
            addContainers.setLong(2, player.getUserID());
            addContainers.addBatch();

            addContainers.executeBatch();


        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        writeItemsToContainer(player, player.getHold());
        writeItemsToContainer(player, player.getLocker());
    }

    public void fetchMails(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM mails" +
                " WHERE players_id=?");)
        {
            ps.setLong(1, player.getUserID());
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final int mailID = resultSet.getInt("mail_id");
                    final long mailTemplateGUID = resultSet.getLong("mail_template_guid");
                    final String rawTimeStamp = resultSet.getString("received_timestamp");
                    final LocalDateTime receivedTimeStamp = LocalDateTime.parse(rawTimeStamp);
                    final String rawParameters = resultSet.getString("parameters");
                    final String[] parameters = rawParameters.split(",");

                    final List<ShipItem> itemsFetched = fetchMailItems(player.getUserID(), mailID);
                    final Mail mail = new Mail(mailID, mailTemplateGUID, Mail.MailStatus.Unread,
                            receivedTimeStamp, itemsFetched, parameters, player.getUserID());
                    player.getMailBox().addItem(mail);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private List<ShipItem> fetchMailItems(final long playerID, final int mailID)
    {
        final List<ShipItem> itemsFetched = new ArrayList<>();
        final ContainerType type = ContainerType.Mail;
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM ship_systems" +
                " WHERE players_id=? AND containers_id=? AND server_id=?");)
        {
            ps.setLong(1, playerID);
            ps.setInt(2, type.value);
            ps.setInt(3, mailID);

            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final long guid = resultSet.getLong("guid");
                    final float durability = resultSet.getFloat("durability");

                    try
                    {
                        final ShipSystem system = ShipSystem.fromGUID(guid);
                        system.setDurability(durability);
                        itemsFetched.add(system);
                    }
                    catch (IllegalArgumentException illegalArgumentException)
                    {
                        log.warn("guid missing " + guid);
                    }

                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM item_countables" +
                " WHERE players_id=? AND containers_id=? AND server_id=?");)
        {
            ps.setLong(1, playerID);
            ps.setInt(2, type.value);
            ps.setInt(3, mailID);
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final long guid = resultSet.getLong("guid");
                    final long count = resultSet.getLong("count");

                    try
                    {
                        final ItemCountable countable = ItemCountable.fromGUID(guid, count);
                        itemsFetched.add(countable);
                    }
                    catch (IllegalArgumentException illegalArgumentException)
                    {
                        log.warn("illegalArgumentException in fetch mail items " + illegalArgumentException.getMessage());
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return itemsFetched;
    }
    public void writeMails(final Player player)
    {
        deleteMailsFromContainers(player);


        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement addContainers = dbConnection.prepareStatement(
                "INSERT INTO containers(container_types_id, players_id)" +
                        "VALUES (?, ?)");)
        {
            //add all
            addContainers.setInt(1, ContainerType.Mail.value);
            addContainers.setLong(2, player.getUserID());
            addContainers.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        writeMails(player.getUserID(), player.getMailBox().getItems().values());
    }
    private void writeMails(final long playerID, final Collection<Mail> mails)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO mails" +
                "(mail_id, players_id, mail_template_guid, received_timestamp, parameters) VALUES" +
                "(?, ?, ?, ?, ?)");)
        {
            for (Mail mail : mails)
            {
                ps.setInt(1, mail.getServerID());
                ps.setLong(2, playerID);
                ps.setLong(3, mail.getMailTemplateCardGuid());
                ps.setString(4, mail.getReceived().toString());

                final StringBuilder parameterString = new StringBuilder();
                String[] parameters = mail.getParameters();
                for (int i = 0; i < parameters.length; i++)
                {
                    final String parameter = parameters[i];
                    if (i != 0)
                    {
                        parameterString.append(",");
                    }
                    parameterString.append(parameter);
                }
                ps.setString(5, parameterString.toString());

                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        writeMailEntries(playerID, mails);
    }
    private void writeMailEntries(final long playerID, final Collection<Mail> mails)
    {
        for (final Mail mail : mails)
        {
            final Mail.MailContainer container = mail.getMailContainer();
            final ContainerType type = container.getContainerID().getContainerType();

            try(var dbConnection = agroalDataSource.getConnection();
                final PreparedStatement shipSystems =
                        dbConnection.prepareStatement("INSERT INTO ship_systems(" +
                                "players_id, containers_id, server_id, guid, durability)" +
                                "VALUES (?, ?, ?, ?, ?)");)
            {
                for (final int itemId : container.getAllItemsIDs())
                {
                    final ShipItem item = container.getByID(itemId);
                    if (item instanceof ShipSystem system)
                    {
                        shipSystems.setLong(1, playerID);
                        shipSystems.setInt(2, type.value);
                        shipSystems.setInt(3, mail.getServerID());
                        shipSystems.setLong(4, system.getCardGuid());
                        shipSystems.setFloat(5, system.getDurability());
                        shipSystems.addBatch();
                    }
                }
                shipSystems.executeBatch();

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
            try(var dbConnection = agroalDataSource.getConnection();
                final PreparedStatement shipCountables =
                        dbConnection.prepareStatement("INSERT INTO item_countables(" +
                                "players_id, containers_id, server_id, guid, count)" +
                                "VALUES (?, ?, ?, ?, ?)");)
            {
                for (final int itemId : container.getAllItemsIDs())
                {
                    final ShipItem item = container.getByID(itemId);
                    if (item instanceof ItemCountable countable)
                    {
                        shipCountables.setLong(1, playerID);
                        shipCountables.setInt(2, type.value);
                        shipCountables.setInt(3, mail.getServerID());
                        shipCountables.setLong(4, countable.getCardGuid());
                        shipCountables.setFloat(5, countable.getCount());
                        shipCountables.addBatch();
                    }
                }
                shipCountables.executeBatch();

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void fetchContainer(final Player player, final IContainer container)
    {
        final ContainerType type = container.getContainerID().getContainerType();
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM ship_systems" +
                " WHERE players_id=? AND containers_id=?");)
        {

            ps.setLong(1, player.getUserID());
            ps.setInt(2, type.value);
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final int serverID = resultSet.getInt("server_id"); //useless since it's set new anyway
                    final long guid = resultSet.getLong("guid");
                    final float durability = resultSet.getFloat("durability");

                    try
                    {
                        final ShipSystem system = ShipSystem.fromGUID(guid);
                        system.setDurability(durability);
                        container.addShipItem(system);
                    }
                    catch (IllegalArgumentException illegalArgumentException)
                    {
                        log.info("in reading item, missing guid " + guid);
                    }
                }
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM item_countables" +
                " WHERE players_id=? AND containers_id=?");)
        {
            ps.setLong(1, player.getUserID());
            ps.setInt(2, type.value);
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final int serverID = resultSet.getInt("server_id"); //useless since it's set new anyway
                    final long guid = resultSet.getLong("guid");
                    final long count = resultSet.getLong("count");
                    if (!gameServerParamsConfig.starterParams().testingMode() && player.getUserID() > 4)
                    {
                        if (guid == ResourceType.Cubits.guid && count >= 2_000_000)
                        {
                            log.warn("Cheat warning; userID: {} has unusual amount of cubits {}", player.getUserID(), count);
                        }
                        else if (guid == ResourceType.TuningKit.guid && count >= 2000)
                        {
                            log.warn("Cheat warning; userID: {} has unusual amount of tuningKits {}", player.getUserID(), count);
                        }
                    }
                    try
                    {
                        final ItemCountable countable = ItemCountable.fromGUID(guid, count);
                        container.addShipItem(countable);
                    }
                    catch (IllegalArgumentException illegalArgumentException)
                    {
                        log.error("in reading item, missing guid " + guid);
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void fetchContainers(final Player player)
    {
        fetchContainer(player, player.getHold());
        fetchContainer(player, player.getLocker());
    }
}
