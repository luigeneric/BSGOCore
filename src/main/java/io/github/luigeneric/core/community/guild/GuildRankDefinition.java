package io.github.luigeneric.core.community.guild;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.GuildRole;

import java.util.Objects;

public class GuildRankDefinition implements IProtocolWrite
{
    /**
     * The id based on the enum id
     */
    private final GuildRole guildRole;
    private String name;
    private long permissions;

    public GuildRankDefinition(final GuildRole guildRole, final String name, final long permissions)
    {
        this.guildRole = Objects.requireNonNull(guildRole, "GuildRole cannot be null!");
        this.name = name;
        this.permissions = permissions;
    }

    public GuildRole getGuildRole()
    {
        return guildRole;
    }

    public String getName()
    {
        return name;
    }

    public long getPermissions()
    {
        return permissions;
    }
    public void setPermissions(final long newPermissions)
    {
        this.permissions = newPermissions;
    }
    public void setName(final String newName)
    {
        this.name = name;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.guildRole.getValue());
        bw.writeString(this.name);
        bw.writeUInt64(this.permissions);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuildRankDefinition that = (GuildRankDefinition) o;

        return guildRole == that.guildRole;
    }

    @Override
    public int hashCode()
    {
        return guildRole.hashCode();
    }
}
