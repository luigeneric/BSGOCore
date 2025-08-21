package io.github.luigeneric.core.community.guild;


import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.enums.GuildOperation;
import io.github.luigeneric.enums.GuildRole;
import io.github.luigeneric.utils.AutoLock;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Guild
{
    private final long id;
    private final String name;
    private final Map<Long, GuildMemberInfo> guildMemberInfos;
    private final Map<GuildRole, GuildRankDefinition> guildGuildRankDefinitions;
    private final GuildSubscriber guildSubscriber;
    private final Lock lock;

    protected Guild(final long id, final String name, final GuildSubscriber guildSubscriber)
    {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name of new guild cannot be nulL!");
        this.guildSubscriber = guildSubscriber;

        this.guildMemberInfos = new HashMap<>();
        this.guildGuildRankDefinitions = new HashMap<>();
        this.lock = new ReentrantLock();
        this.setupDefaultRankDefinitions();
    }

    public void addMember(final Player player, final GuildRole guildRole)
    {
        try(var l = new AutoLock(lock))
        {
            final BgoTimeStamp lastLogout = player.getLastLogout().orElse(new BgoTimeStamp(System.currentTimeMillis()));
            this.guildMemberInfos.put(player.getUserID(),
                    new GuildMemberInfo(player.getUserID(), player.getName(), player.getSkillBook().get(),
                            guildRole, lastLogout.getLocalDate(), player.getLocation()));
        }
    }


    private void setupDefaultRankDefinitions()
    {
        for (final GuildRole guildRole : GuildRole.values())
        {
            if (guildRole == GuildRole.Leader)
            {
                long bitMask = 0;
                for (GuildOperation guildOperation : GuildOperation.values())
                {
                    bitMask |= guildOperation.getBitmask();
                }
                this.guildGuildRankDefinitions.put(guildRole, new GuildRankDefinition(guildRole, guildRole.name(), bitMask));
            }
            else
            {
                this.guildGuildRankDefinitions.put(guildRole, new GuildRankDefinition(guildRole, guildRole.name(), 0));
            }
        }
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public List<GuildMemberInfo> getGuildMemberInfos()
    {
        return new ArrayList<>(guildMemberInfos.values());
    }
    public List<GuildRankDefinition> getGuildRankDefinitions()
    {
        return new ArrayList<>(guildGuildRankDefinitions.values());
    }

    /**
     * Removes first player from the guild if present
     * @param id the id of the player
     * @return true if the player was present
     */
    public boolean removePlayer(final long id)
    {
        try(var l = new AutoLock(lock))
        {
            final GuildMemberInfo existing = this.guildMemberInfos.remove(id);
            if (this.guildMemberInfos.isEmpty())
                this.guildSubscriber.notifyMemberCountZero(this.id);
            return existing != null;
        }
    }

    public Optional<GuildMemberInfo> getGuildMemberInfoOf(final long userID)
    {
        return Optional.ofNullable(this.guildMemberInfos.get(userID));
    }

    public static boolean hasPermissionFor(final GuildRankDefinition guildRankDefinition, final GuildOperation permission)
    {
        final int num = (permission.shortValue - GuildOperation.ChangePermissions.shortValue);
        if (num < 0)
            return false;
        long num2 = 1L << num;
        if (guildRankDefinition == null)
            return false;
        return (guildRankDefinition.getPermissions() & num2) != 0;
    }

    public boolean hasPermissions(final long playerID, final GuildOperation operation)
    {
        try(var l = new AutoLock(lock))
        {
            final GuildMemberInfo memberInfo = this.guildMemberInfos.get(playerID);
            if (memberInfo == null)
                return false;
            final GuildRankDefinition rankDefinition = this.guildGuildRankDefinitions.get(memberInfo.getPlayerRole());
            return hasPermissionFor(rankDefinition, operation);
        }
    }
    public void changePermissionsOfRole(final GuildRole guildRole, final long permissions)
    {
        try(var l = new AutoLock(lock))
        {
            final GuildRankDefinition rankDefinition = this.guildGuildRankDefinitions.get(guildRole);
            if (rankDefinition == null)
                return;
            rankDefinition.setPermissions(permissions);
        }
    }

    public boolean changeRankName(final GuildRole guildRole, final String roleName)
    {
        try(var l = new AutoLock(lock))
        {
            final GuildRankDefinition rankDefinition = this.guildGuildRankDefinitions.get(guildRole);
            if (rankDefinition == null)
                return false;
            rankDefinition.setName(roleName);
            return true;
        }
    }
    public void injectRankDefinitions(final Collection<GuildRankDefinition> toInject)
    {
        try(var l = new AutoLock(lock))
        {
            for (final GuildRankDefinition rankDefinition : toInject)
            {
                this.guildGuildRankDefinitions.put(rankDefinition.getGuildRole(), rankDefinition);
            }
        }
    }

    public GuildMemberInfo appointNewLeaderIfLeaderNotPresent()
    {
        try(var l = new AutoLock(lock))
        {
            final boolean hasLeader = this.guildMemberInfos.values().stream()
                    .anyMatch(memberInfo -> memberInfo.getPlayerRole() == GuildRole.Leader);
            if (hasLeader)
                return null;
            //get highest role
            final List<GuildMemberInfo> sorted = this.guildMemberInfos.values().stream()
                    .sorted(new Comparator<GuildMemberInfo>()
                    {
                        @Override
                        public int compare(GuildMemberInfo o1, GuildMemberInfo o2)
                        {
                            return -Byte.compare(o1.getPlayerRole().getValue(), o2.getPlayerRole().getValue());
                        }
                    })
                    .toList();
            if (sorted.isEmpty())
                return null;
            final GuildMemberInfo nextHighest = sorted.get(0);
            nextHighest.setPlayerRole(GuildRole.Leader);
            return nextHighest;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Guild guild = (Guild) o;

        if (id != guild.id) return false;
        return name.equals(guild.name);
    }

    @Override
    public int hashCode()
    {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Guild{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", memberCount='" + this.guildMemberInfos.size() + '\'' +
                '}';
    }
}
