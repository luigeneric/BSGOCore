package io.github.luigeneric.database.guilds;


import io.agroal.api.AgroalDataSource;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.community.guild.GuildMemberInfo;
import io.github.luigeneric.core.community.guild.GuildRankDefinition;
import io.github.luigeneric.core.community.guild.GuildRegistry;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.database.SqLiteProvider;
import io.github.luigeneric.enums.GuildRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SqLiteGuildProcessor
{
    private final AgroalDataSource agroalDataSource;
    private final SqLiteProvider sqLiteProvider;


    public void deleteGuild(final long guildID)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM guilds WHERE id=?");)
        {
            ps.setLong(1, guildID);
            ps.executeUpdate();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM guild_rank_definitions WHERE guild_id=?");)
        {
            ps.setLong(1, guildID);
            ps.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM guild_member_infos WHERE guild_id=?");)
        {
            ps.setLong(1, guildID);
            ps.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    private void writeRankDefinitions(final long guildID, final List<GuildRankDefinition> guildRankDefinitions)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO " +
                "guild_rank_definitions(guild_id, role_id, rank_name, permissions) VALUES" +
                "(?, ?, ?, ?)");)
        {
            for (final GuildRankDefinition guildRankDefinition : guildRankDefinitions)
            {
                ps.setLong(1, guildID);
                ps.setByte(2, guildRankDefinition.getGuildRole().getValue());
                ps.setString(3, guildRankDefinition.getName());
                ps.setLong(4, guildRankDefinition.getPermissions());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
        }
    }
    private void writeGuildMemberInfos(final long guildID, final List<GuildMemberInfo> guildMemberInfos)
    {
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO " +
                "guild_member_infos(guild_id, players_id, guild_roles_id) VALUES" +
                "(?, ?, ?)");)
        {
            for (final GuildMemberInfo memberInfo : guildMemberInfos)
            {
                ps.setLong(1, guildID);
                ps.setLong(2, memberInfo.getPlayerID());
                ps.setByte(3, memberInfo.getPlayerRole().getValue());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    @Transactional
    public void writeAllGuilds(final GuildRegistry guildRegistry)
    {
        sqLiteProvider.lockWrite();
        log.info("guild lock aquired, starting to write guilds");
        try(var dbConnection = agroalDataSource.getConnection())
        {
            final List<Guild> allGuilds = guildRegistry.getAllGuilds();
            for (final Guild guild : allGuilds)
            {
                deleteGuild(guild.getId());
            }
            try(final PreparedStatement ps = dbConnection.prepareStatement("REPLACE INTO guilds(id, name) VALUES" +
                    "(?, ?)");)
            {
                for (int i = 0, allGuildsSize = allGuilds.size(); i < allGuildsSize; i++)
                {
                    Guild guild = allGuilds.get(i);
                    log.info("writeGuild {} into db", guild);
                    ps.setLong(1, guild.getId());
                    ps.setString(2, guild.getName());
                    ps.addBatch();
                    if (i % 10 == 0)
                    {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }

            allGuilds.forEach(guild -> writeRankDefinitions(guild.getId(), guild.getGuildRankDefinitions()));
            allGuilds.forEach(guild -> writeGuildMemberInfos(guild.getId(), guild.getGuildMemberInfos()));
        } catch (SQLException e)
        {
            log.error("error in writing guild to db", e);
        } finally
        {
            sqLiteProvider.releaseWrite();
            log.info("release guild write lock");
        }
    }

    public void fetchAllGuildsSaved(final GuildRegistry guildRegistry)
    {
        final List<GuildFetchResult> guildFetchResults = new ArrayList<>();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM guilds");
            final ResultSet resultSet = ps.executeQuery();)
        {
            while (resultSet.next())
            {
                final long id = resultSet.getLong("id");
                final String name = resultSet.getString("name");

                final GuildFetchResult guildFetchResult = fetchGuild(id, name);
                guildFetchResults.add(guildFetchResult);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }


        createGuildsFromWrappers(guildRegistry, guildFetchResults);
    }
    private void createGuildsFromWrappers(final GuildRegistry guildRegistry, final List<GuildFetchResult> guildFetchResults)
    {
        for (final GuildFetchResult guildFetchResult : guildFetchResults)
        {
            final Guild guild = guildRegistry.createExistingGuild(guildFetchResult.name(), guildFetchResult.id());
            guild.injectRankDefinitions(guildFetchResult.guildRankDefinitionSet());
            for (final MemberInfoRecord memberInfoRecord : guildFetchResult.memberInfoRecords())
            {
                guild.addMember(memberInfoRecord.player(), memberInfoRecord.guildRole());
            }
        }
    }

    public GuildFetchResult fetchGuild(final long id, final String name)
    {
        final Set<GuildRankDefinition> rankDefinitions = getRankDefinitions(id);

        final Set<MemberInfoRecord> guildMemberInfos = fetchGuildMemberInfos(id);

        return new GuildFetchResult(id, name, rankDefinitions, guildMemberInfos);
    }

    private Set<GuildRankDefinition> getRankDefinitions(final long guildID)
    {
        final Set<GuildRankDefinition> guildRankDefinitions = new HashSet<>();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM guild_rank_definitions WHERE guild_id=?");)
        {
            ps.setLong(1, guildID);

            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final byte roleId = resultSet.getByte("role_id");
                    final String rankName = resultSet.getString("rank_name");
                    final long permissions = resultSet.getLong("permissions");
                    guildRankDefinitions.add(new GuildRankDefinition(GuildRole.forValue(roleId), rankName, permissions));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return guildRankDefinitions;
    }

    private Set<MemberInfoRecord> fetchGuildMemberInfos(final long guildID)
    {
        final Set<MemberInfoRecord> memberInfoRecords = new HashSet<>();
        try(var dbConnection = agroalDataSource.getConnection();
            final PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM guild_member_infos WHERE guild_id=?");)
        {
            ps.setLong(1, guildID);

            try(final ResultSet resultSet = ps.executeQuery();)
            {
                while (resultSet.next())
                {
                    final long playersId = resultSet.getLong("players_id");
                    final byte rolesId = resultSet.getByte("guild_roles_id");
                    final Player player = this.sqLiteProvider.fetchPlayer(playersId);
                    memberInfoRecords.add(new MemberInfoRecord(GuildRole.forValue(rolesId), player));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return memberInfoRecords;
    }


}
