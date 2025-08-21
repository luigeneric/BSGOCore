package io.github.luigeneric.database;

import io.agroal.api.AgroalDataSource;
import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.MissionUpdater;
import io.github.luigeneric.core.community.guild.GuildRegistry;
import io.github.luigeneric.core.database.CounterRecord;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.player.*;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.player.counters.CounterDesc;
import io.github.luigeneric.core.player.counters.Counters;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.factors.Factor;
import io.github.luigeneric.core.player.factors.Factors;
import io.github.luigeneric.core.player.settings.*;
import io.github.luigeneric.core.player.settings.values.*;
import io.github.luigeneric.core.player.skills.PlayerSkill;
import io.github.luigeneric.core.player.skills.SkillBook;
import io.github.luigeneric.core.protocols.setting.SettingProtocol;
import io.github.luigeneric.database.fetchresults.GameLocationFetchResult;
import io.github.luigeneric.database.fetchresults.HangarInfoFetchResult;
import io.github.luigeneric.database.fetchresults.ShipInfoFetchResult;
import io.github.luigeneric.database.fetchresults.ShipSlotInfoFetchResult;
import io.github.luigeneric.database.guilds.SqLiteGuildProcessor;
import io.github.luigeneric.database.missions.SqLiteMissions;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.templates.utils.MapStarDesc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@ApplicationScoped
public class SqLiteProvider implements DbProvider
{
    private final AgroalDataSource agroalDataSource;
    private final SqlLiteContainers containers;
    private final SqLiteHangar hangars;
    private final SqLiteGuildProcessor sqLiteGuildProcessor;
    private final GameServerParamsConfig gameServerParamsConfig;
    private final GalaxyMapCard galaxyMapCard;
    private final SqLiteMissions sqLiteMissions;
    protected final ReadWriteLock readWriteLock;
    private final MissionUpdater missionUpdater;

    public SqLiteProvider(final AgroalDataSource agroalDataSource,
                          final GameServerParamsConfig gameServerParamsConfig,
                          final MissionUpdater missionUpdater,
                          final Catalogue catalogue,
                          final SqLiteHangar sqLiteHangar,
                          final SqlLiteContainers containers,
                          final SqLiteMissions sqLiteMissions,
                          final SqLiteGuildProcessor guildProcessor
    ) throws SQLException
    {
        this.agroalDataSource = agroalDataSource;
        this.missionUpdater = missionUpdater;
        this.gameServerParamsConfig = gameServerParamsConfig;
        this.galaxyMapCard = catalogue.galaxyMapCard();
        this.readWriteLock = new ReentrantReadWriteLock();

        this.containers = containers;
        this.hangars = sqLiteHangar;
        this.sqLiteMissions = sqLiteMissions;
        this.sqLiteGuildProcessor = guildProcessor;
    }

    public void lockRead()
    {
        this.readWriteLock.readLock().lock();
    }
    public void lockWrite()
    {
        this.readWriteLock.writeLock().lock();
    }
    public void releaseRead()
    {
        this.readWriteLock.readLock().unlock();
    }
    public void releaseWrite()
    {
        this.readWriteLock.writeLock().unlock();
    }

    @Override
    public AvatarDescription fetchAvatarDescription(long userId)
    {
        return null;
    }

    @Override
    @Transactional
    public Player fetchPlayer(final long userId)
    {
        lockRead();
        try(
                var dbConnection = agroalDataSource.getConnection();
                PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM players WHERE id=?");)
        {
            final Player player = new Player(userId, gameServerParamsConfig, new MissionBook(userId, missionUpdater));

            ps.setLong(1, userId);
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                final boolean hasNext = resultSet.next();
                if (hasNext)
                {
                    final String name = resultSet.getString("name");
                    final Faction faction = Faction.valueOf(resultSet.getByte("faction_id"));
                    final long roleBits = resultSet.getLong("roles_bits");
                    final String rawDate = resultSet.getString("last_logout_date");
                    if (!rawDate.equals(""))
                    {
                        player.setLastLogout(new BgoTimeStamp(LocalDateTime.parse(rawDate)));
                    }
                    final String rawWofDate = resultSet.getString("last_wof_date");
                    final LocalDateTime lastWofDate = parseDateTimeFromString(rawWofDate);

                    player.setName(name);
                    player.setFaction(faction);
                    player.setupBasicHangar();
                    player.getBgoAdminRoles().setOr(roleBits);
                    player.setLastFreeWofGame(lastWofDate);
                }
            }

            final Map<AvatarItem, String> avatarItems = fetchAvatar(userId);
            final AvatarDescription description = player.getAvatarDescription().get();
            description.injectNewAvatarDescription(avatarItems);

            final GameLocationFetchResult existingLocation = fetchLocation(userId, player.getFaction());
            if (existingLocation == null)
            {
                throw new IllegalStateException("Player fetching location but location was null!");
            }
            else
            {
                final GameLocation locationToSet = existingLocation.previousLocation();
                MapStarDesc star = galaxyMapCard.getStars().get(existingLocation.sectorID());
                if (star == null)
                {
                    star = galaxyMapCard.getStarterSectorForFaction(player.getFaction());
                }
                player.getLocation().setLocation(locationToSet, star.getId(), star.getSectorGuid());
            }

            final HangarInfoFetchResult hangarInfo = hangars.fetchHangar(player.getUserID());
            final Hangar hangar = player.getHangar();
            for (final ShipInfoFetchResult shipInfoFetchResult : hangarInfo.shipInfoFetchResults())
            {
                final HangarShip ship = new HangarShip(userId, shipInfoFetchResult.serverID(), shipInfoFetchResult.guid(), shipInfoFetchResult.name());
                ship.setDurability(shipInfoFetchResult.durability());
                final ShipSlots slots = ship.getShipSlots();

                for (final ShipSlotInfoFetchResult slotInfo : shipInfoFetchResult.slotInfoWrappers())
                {
                    //no system inside the slot!
                    if (slotInfo.guid() == 0)
                        continue;

                    try
                    {
                        final ShipSystem system = ShipSystem.fromGUID(slotInfo.guid());
                        system.setDurability(slotInfo.durability());
                        final ShipSlot slot = slots.getSlot(slotInfo.serverID());
                        if (slot != null)
                            slot.addShipItem(system);
                    }
                    catch (final IllegalArgumentException illegalArgumentException)
                    {
                        log.warn("IllegalArgumentException fetching system from db " + illegalArgumentException.getMessage());
                    }

                }
                ship.getShipStats().setMaxHpPp();
                hangar.addHangarShip(ship);

            }
            hangar.setActiveShipIndex(hangarInfo.activeIndex());


            fetchSkillBook(player);
            containers.fetchContainers(player);
            containers.fetchMails(player);
            fetchSettings(player);
            fetchCounters(player);
            fetchTokenCap(player);
            fetchFactors(player);
            sqLiteMissions.fetchMissionBook(player);

            final var stats = hangar.getActiveShip().getShipStats();
            stats.setMaxHp();

            return player;
        } catch (SQLException e)
        {
            log.error("ERROR in writing to the database! " + e.getMessage() + " userID:" + userId);
            throw new RuntimeException(e);
        } finally
        {
            releaseRead();
        }
    }

    private void fetchFactors(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM factor WHERE players_id=?");)
        {
            ps.setLong(1, player.getUserID());
            try(final ResultSet resultSet = ps.executeQuery())
            {
                while (resultSet.next())
                {
                    final int factorSourceId = resultSet.getInt("factor_source_id");
                    final int factorTypeId = resultSet.getInt("factor_type_id");
                    final float value = resultSet.getFloat("value");
                    final String rawEndTime = resultSet.getString("end_time");

                    final LocalDateTime endTime = LocalDateTime.parse(rawEndTime);
                    final boolean factorIsValid = endTime.isAfter(LocalDateTime.now(Clock.systemUTC()));
                    if (factorIsValid)
                    {
                        var tmpFactor = new Factor(0, FactorType.forValue(factorTypeId), FactorSource.forValue(factorSourceId),
                                value, endTime);
                        player.getFactors().addFactor(tmpFactor);
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void writeTokenCap(final Player player)
    {
        final ResourceCap tokenCap = player.getMeritsCapFarmed();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO caps(players_id, guid, value, last_cap_date)" +
                " VALUES (?, ?, ?, ?)");)
        {
            ps.setLong(1, player.getUserID());
            ps.setLong(2, tokenCap.getGuid());
            ps.setInt(3, tokenCap.getFarmed());
            ps.setString(4, tokenCap.getLastReset().getLocalDate().toString());
            ps.executeUpdate();
        } catch (SQLException e)
        {
            //TODO add catch
            e.printStackTrace();
        }
    }
    private void fetchTokenCap(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM caps WHERE players_id=?"))
        {
            ps.setLong(1, player.getUserID());
            try(final ResultSet resultSet = ps.executeQuery())
            {
                while (resultSet.next())
                {
                    final long guid = resultSet.getLong("guid");
                    final int value = resultSet.getInt("value");
                    final String rawLastCapDate = resultSet.getString("last_cap_date");

                    final LocalDateTime oldCapDate = LocalDateTime.parse(rawLastCapDate);
                    final boolean isToday = LocalDateTime.now(Clock.systemUTC()).getDayOfYear() == oldCapDate.getDayOfYear();
                    if (isToday)
                    {
                        final ResourceCap cap = player.getMeritsCapFarmed();
                        cap.setCap(value, oldCapDate);
                    }

                    player.getCounterFacade().counters().injectOldCounters(guid, value);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fetchCounters(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM counters WHERE players_id=?");)
        {
            ps.setLong(1, player.getUserID());
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final long guid = resultSet.getLong("guid");
                    final double value = resultSet.getDouble("value");

                    player.getCounterFacade().counters().injectOldCounters(guid, value);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private Map<AvatarItem, String> fetchAvatar(final long playerID)
    {
        final Map<AvatarItem, String> avatarItems = new HashMap<>();
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM avatar_items WHERE players_id=?");)
        {
            ps.setLong(1, playerID);
            try(final ResultSet resultSet = ps.executeQuery())
            {
                while (resultSet.next())
                {
                    final int avatarItemID = resultSet.getInt("item_id");
                    final AvatarItem avatarItem = AvatarItem.forValue(avatarItemID);
                    final String value = resultSet.getString("value");

                    avatarItems.put(avatarItem, value);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return avatarItems;
    }
    private GameLocationFetchResult fetchLocation(final long playerID, final Faction faction)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM locations WHERE players_id=?");)
        {
            ps.setLong(1, playerID);
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                final boolean exists = resultSet.next();
                if (!exists)
                {
                    final MapStarDesc starterSector = galaxyMapCard.getStarterSectorForFaction(faction);
                    return new GameLocationFetchResult(starterSector.getId(), GameLocation.Room, GameLocation.Starter);
                }
                final long sectorID = resultSet.getLong("sector_id");
                final GameLocation gameLocation = GameLocation.forValue(resultSet.getByte("game_locations_id"));
                final GameLocation previousLocation = GameLocation.forValue(resultSet.getByte("previous_game_locations_id"));

                return new GameLocationFetchResult(sectorID, gameLocation, previousLocation);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @SneakyThrows
    public boolean checkIfUserExists(final long userId)
    {
        log.info("Checking if user exists in db: {}", userId);
        lockRead();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM players WHERE id=? LIMIT 1"))
        {
            ps.setLong(1, userId);
            try(final ResultSet resultSet = ps.executeQuery();)
            {
                return resultSet.next();
            }
        }
        finally
        {
            releaseRead();
        }
    }

    @Override
    @Transactional
    public void updateAvatarDescription(long userId, AvatarDescription avatarDescription)
    {
        writeAvatar(avatarDescription, userId);
    }

    @Override
    public boolean checkNameAlreadyPresent(final String name)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM players WHERE name=?"))
        {
            ps.setString(1, name);
            try(final ResultSet resultSet = ps.executeQuery())
            {
                return resultSet.next();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public boolean checkNameAlreadyPresentNoCase(final String name)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM players WHERE name COLLATE NOCASE = ?"))
        {
            ps.setString(1, name);
            try(final ResultSet resultSet = ps.executeQuery())
            {
                return resultSet.next();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    @Transactional
    public void bulkWritePlayerToDb(final Collection<Player> players)
    {
        lockWrite();
        try
        {
            log.info("Bulk writeup started, size: " + players.size());
            for (final Player player : players)
            {
                internalWritePlayer(player);
            }
            log.info("Bulk writeup finished");
        }
        finally
        {
            releaseWrite();
        }
    }
    @Override
    @Transactional
    public void writePlayerToDb(final Player player)
    {
        lockWrite();
        try
        {
            internalWritePlayer(player);
        }
        finally
        {
            releaseWrite();
        }
    }

    private void internalWritePlayer(final Player player)
    {
        Objects.requireNonNull(player, "Player to write to db cannot be null!");

        final GameLocation location = player.getLocation().getNonDisconnectLocation();
        if (location == GameLocation.Starter || location == GameLocation.Avatar)
        {
            log.info("User is in " + location + " stop write");
            return;
        }

        try(var dbConnection = agroalDataSource.getConnection())
        {
            log.info("Writing player to db... " + player.getPlayerLog());
            //usertable
            try(final PreparedStatement ps = dbConnection.prepareStatement(
                    "REPLACE INTO players" +
                            "(id, name, faction_id, roles_bits, last_logout_date, last_wof_date)" +
                            " VALUES (?, ?, ?, ?, ?, ?)")
            )
            {
                ps.setLong(1, player.getUserID());
                ps.setString(2, player.getName());
                ps.setByte(3, player.getFaction().value);
                ps.setInt(4, player.getBgoAdminRoles().getRoleBits());
                String lastLogoutText = "";
                if (player.getLastLogout().isPresent())
                {
                    lastLogoutText = player.getLastLogout().get().getLocalDate().toString();
                }
                ps.setString(5, lastLogoutText);
                String wofDrawDate = "";
                if (player.getLastFreeWofGame().isPresent())
                {
                    wofDrawDate = player.getLastFreeWofGame().get().getLocalDate().toString();
                }

                ps.setString(6, wofDrawDate);
                ps.executeUpdate();
            } catch (SQLException e)
            {
                //TODO add catch
                e.printStackTrace();
            }

            writeAvatar(player.getAvatarDescription().get(), player.getUserID());
            writeSettings(player);
            writeLocation(player);
            writeSkillBook(player);
            sqLiteMissions.writeMissionsToDb(player);

            try
            {
                this.hangars.writeHangar(player);
            }
            catch (IllegalCallerException illegalCallerException)
            {
                //no hangarships yet!
            }
            writeCountersDb(player);


            containers.writeContainers(player);
            containers.writeMails(player);
            writeTokenCap(player);
            writeFactors(player);
            log.info("Write player " + player.getName() + " to database finished");
        }
        catch (SQLException e)
        {
            log.error("Error writing player to db: " + e.getMessage());
        }

    }

    private void writeFactors(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement deletePs = dbConnection.prepareStatement("DELETE FROM factor WHERE players_id=?");
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO factor" +
                    "(id, players_id, factor_source_id, factor_type_id, value, end_time)" +
                    " VALUES (?, ?, ?, ?, ?, ?)");
        )
        {
            deletePs.setLong(1, player.getUserID());
            deletePs.executeUpdate();

            final Factors factors = player.getFactors();
            for (final Factor factor : factors.values())
            {
                if (factor.getFactorSource() == FactorSource.Faction)
                    continue;

                ps.setInt(1, factor.getServerID());
                ps.setLong(2, player.getUserID());
                ps.setInt(3, factor.getFactorSource().intValue);
                ps.setInt(4, factor.getFactorType().intValue);
                ps.setFloat(5, factor.getValue());
                ps.setString(6, factor.getEndTime().toString());

                ps.addBatch();
            }
            ps.executeBatch();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void writeGuilds(final GuildRegistry guildRegistry)
    {
        log.info("try write guilds");
        sqLiteGuildProcessor.writeAllGuilds(guildRegistry);
    }

    @Override
    public void fetchGuilds(final GuildRegistry guildRegistry)
    {
        sqLiteGuildProcessor.fetchAllGuildsSaved(guildRegistry);
    }

    @Override
    public Map<Long, CounterRecord> fetchAllCounters()
    {
        final Map<Long, CounterRecord> counterDescMap = new HashMap<>();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM counters");
            final ResultSet rs = ps.executeQuery();)
        {
            while (rs.next())
            {
                final long playerID = rs.getLong(1);
                final long guid = rs.getLong(2);
                final double value = rs.getDouble(3);
                CounterRecord counterRecord = counterDescMap.get(playerID);
                if (counterRecord == null)
                {
                    counterRecord = new CounterRecord(playerID);
                }
                counterRecord.counters().put(guid, value);
                counterDescMap.put(playerID, counterRecord);
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return counterDescMap;
    }

    private void writeAvatar(final AvatarDescription avatarDescription, final long userID)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO avatar_items(players_id, item_id, value)" +
                " VALUES (?, ?, ?)");)
        {
            final Map<AvatarItem, String> items = avatarDescription.getUnmodifiableItems();

            for (final Map.Entry<AvatarItem, String> item : items.entrySet())
            {
                ps.setLong(1, userID);
                ps.setInt(2, item.getKey().getValue());
                ps.setString(3, item.getValue());

                ps.addBatch();
            }
            ps.executeBatch();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void writeCountersDb(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            PreparedStatement deleteS = dbConnection.prepareStatement("DELETE FROM counters WHERE players_id = ?");
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO counters(players_id, guid, value)" +
                    " VALUES (?, ?, ?)");
            )
        {
            deleteS.setLong(1, player.getUserID());
            deleteS.executeUpdate();


            final Counters counters = player.getCounterFacade().counters();
            int count = 0;
            for (final CounterDesc value : counters.getInternalReadOnly().values())
            {
                ps.setLong(1, player.getUserID());
                ps.setLong(2, value.getGuid());
                ps.setDouble(3, value.getValue());

                if (value.getGuid() == 130920111)
                {
                    log.error("Counter error, token written as counter from {}", player.getPlayerLog());
                    continue;
                }

                ps.addBatch();
                if (count % 50 == 0)
                {
                    ps.executeBatch();
                }
                count++;
            }
            ps.executeBatch();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fetchSkillBook(final Player player)
    {
        final SkillBook emptySkillBook = player.getSkillBook();

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement bookStatement = dbConnection.prepareStatement("SELECT * FROM skillbooks WHERE player_id = ?");)
        {
            bookStatement.setLong(1, player.getUserID());
            try(final ResultSet resultSet = bookStatement.executeQuery();)
            {
                final boolean hasEntry = resultSet.next();
                if (hasEntry)
                {
                    final long xp = resultSet.getLong("xp");
                    final long spentXP = resultSet.getLong("spent_xp");
                    emptySkillBook.setExperience(0);

                    emptySkillBook.addExperience(xp);
                    emptySkillBook.setSpentExperience(spentXP);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement skillsStatement = dbConnection.prepareStatement("SELECT * FROM player_skills WHERE player_id = ?");)
        {
            skillsStatement.setLong(1, player.getUserID());
            try(final ResultSet resultSet = skillsStatement.executeQuery();)
            {
                final List<Long> skillCardGuids = new ArrayList<>();
                while (resultSet.next())
                {
                    final long cardGUID = resultSet.getLong("card_guid");
                    final int serverID = resultSet.getInt("server_id");
                    final PlayerSkill skill = emptySkillBook.getAllSkills().get(serverID);
                    skill.injectSkillCard(cardGUID);
                }
            }
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
        }
    }
    private void writeSkillBook(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement skillBookStatement = dbConnection.prepareStatement("REPLACE INTO skillbooks" +
                "(player_id, xp, spent_xp)" +
                " VALUES (?, ?, ?)");
            final PreparedStatement skillsStateMent = dbConnection.prepareStatement("REPLACE INTO player_skills" +
                    "(player_id, card_guid, server_id)" +
                    " VALUES (?, ?, ?)");
        )
        {

            skillBookStatement.setLong(1, player.getUserID());
            skillBookStatement.setLong(2, player.getSkillBook().getExperience());
            skillBookStatement.setLong(3, player.getSkillBook().getSpentExperience());
            skillBookStatement.executeUpdate();


            int counter = 0;
            for (final Map.Entry<Integer, PlayerSkill> skillEntry : player.getSkillBook().getAllSkills().entrySet())
            {
                skillsStateMent.setLong(1, player.getUserID());
                skillsStateMent.setLong(2, skillEntry.getValue().getSkillCard().getCardGuid());
                skillsStateMent.setInt(3, skillEntry.getValue().getServerID());
                skillsStateMent.addBatch();
                counter++;
                if (counter % 10 == 0)
                {
                    skillsStateMent.executeBatch();
                }
            }
            skillsStateMent.executeBatch();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void writeLocation(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement locationsStatement = dbConnection.prepareStatement("REPLACE INTO locations" +
                "(players_id, sector_id, game_locations_id, previous_game_locations_id)" +
                " VALUES (?, ?, ?, ?)");)
        {
            locationsStatement.setLong(1, player.getUserID());
            locationsStatement.setLong(2, player.getLocation().getSectorID());
            locationsStatement.setByte(3, player.getLocation().getGameLocation().getValue());
            locationsStatement.setByte(4, player.getLocation().getPreviousLocation().getValue());

            locationsStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void fetchSettings(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement playerSettingsStatement = dbConnection.prepareStatement("SELECT * FROM players_settings_value_bytes" +
                " WHERE players_id=?");)
        {
            playerSettingsStatement.setLong(1, player.getUserID());
            try(final ResultSet resultSet = playerSettingsStatement.executeQuery();)
            {
                final Settings settings = player.getSettings();
                final UserSettings serverSavedSettings = settings.getServerSavedUserSettings();
                while (resultSet.next())
                {
                    final UserSetting setting = UserSetting.forValue(resultSet.getByte("user_setting_id"));
                    final UserSettingValueType valueType = SettingProtocol.getValueType(setting);
                    switch (valueType)
                    {
                        case Byte ->
                        {
                            serverSavedSettings.put(setting, new UserSettingByte((byte) resultSet.getFloat("value")));
                        }
                        case Integer ->
                        {
                            serverSavedSettings.put(setting, new UserSettingInteger((int) resultSet.getFloat("value")));
                        }
                        case Float ->
                        {
                            serverSavedSettings.put(setting, new UserSettingFloat(resultSet.getFloat("value")));
                        }
                        case Boolean ->
                        {
                            final float floatValue = resultSet.getFloat("value");
                            final boolean value = floatValue == 1.0f;
                            serverSavedSettings.put(setting, new UserSettingBoolean(value));
                        }
                        default -> throw new IllegalStateException("SQL FETCHING: type " + valueType + " not implemented!");
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement inputBindingStatement = dbConnection.prepareStatement("SELECT * FROM input_bindings_values" +
                " WHERE players_id=?");)
        {

            inputBindingStatement.setLong(1, player.getUserID());
            try(final ResultSet resultSet = inputBindingStatement.executeQuery();)
            {
                final Settings settings = player.getSettings();
                final InputBindings inputBindings = settings.getInputBindings();

                while (resultSet.next())
                {
                    final Action action = Action.forValue(resultSet.getInt("action_id"));
                    final int triggerCode = resultSet.getInt("device_trigger_code");
                    final short modCode = resultSet.getShort("device_mod_code");
                    final short device = resultSet.getShort("device");
                    final short flags = resultSet.getShort("flags");
                    final short profileNo = resultSet.getShort("profile_number");
                    final InputBinding binding = new InputBinding(action, triggerCode, modCode, device, flags, profileNo);
                    inputBindings.set(binding);
                }
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void writeSettings(final Player player)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement settingsValueBytes = dbConnection.prepareStatement("REPLACE INTO players_settings_value_bytes" +
                "(players_id, user_setting_id, value)" +
                " VALUES (?, ?, ?)");
        )
        {
            final Settings settings = player.getSettings();


            final Map<UserSetting, UserSettingValue<?>> settingsMap = settings.getServerSavedUserSettings().getSettingsUnmodifiableMap();
            for (Map.Entry<UserSetting, UserSettingValue<?>> settingEntry : settingsMap.entrySet())
            {
                if (settingEntry.getValue().getType() == UserSettingValueType.Float2 ||
                settingEntry.getValue().getType() == UserSettingValueType.HelpScreenType)
                {
                    continue;
                }
                settingsValueBytes.setLong(1, player.getUserID());
                settingsValueBytes.setByte(2, settingEntry.getKey().value);
                switch (settingEntry.getValue().getType())
                {
                    case Byte ->
                    {
                        settingsValueBytes.setByte(3, (byte) settingEntry.getValue().getValue());
                    }
                    case Boolean ->
                    {
                        final boolean value = (boolean) settingEntry.getValue().getValue();
                        final byte byteValue = (byte) (value ? 1 : 0);
                        settingsValueBytes.setByte(3, byteValue);
                    }
                    case Integer ->
                    {
                        settingsValueBytes.setInt(3, (int) settingEntry.getValue().getValue());
                    }
                    case Float ->
                    {
                        settingsValueBytes.setFloat(3, (float) settingEntry.getValue().getValue());
                    }
                    case Float2, HelpScreenType ->
                    {
                        //ignore since it's not part of the game anymore!
                    }
                    default -> throw new IllegalArgumentException(
                            "Error inside sqlProvider, passing settings to the db " + settingEntry.getValue().getType() + " not supported!"
                    );
                }
                settingsValueBytes.addBatch();
            }
            final int[] settingsRv = settingsValueBytes.executeBatch();

            try(final PreparedStatement settingsInputBindingsValues = dbConnection.prepareStatement("REPLACE INTO input_bindings_values" +
                    "(players_id, action_id, device_trigger_code, device_mod_code, device, flags, profile_number)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)");)
            {
                final Collection<InputBinding> bindings = settings.getInputBindings().getUnmodifiableInputBindings();
                int counter = 0;
                for (InputBinding binding : bindings)
                {
                    settingsInputBindingsValues.setLong(1, player.getUserID());
                    settingsInputBindingsValues.setInt(2, binding.getAction().intValue);
                    settingsInputBindingsValues.setInt(3, binding.getDeviceTriggerCode());
                    settingsInputBindingsValues.setShort(4, binding.getDeviceModifierCode());
                    settingsInputBindingsValues.setShort(5, binding.getDevice());
                    settingsInputBindingsValues.setShort(6, binding.getFlags());
                    settingsInputBindingsValues.setShort(7, binding.getProfileNo());

                    settingsInputBindingsValues.addBatch();
                    counter++;
                    //execute every 20 entry batch update for safety
                    if (counter % 20 == 0)
                    {
                        settingsInputBindingsValues.executeBatch();
                    }
                }
                settingsInputBindingsValues.executeBatch();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static LocalDateTime parseDateTimeFromString(final String rawStr)
    {
        return rawStr.equals("") ?
                        LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC) :
                        LocalDateTime.parse(rawStr);
    }
}
