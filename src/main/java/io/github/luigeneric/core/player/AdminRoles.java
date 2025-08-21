package io.github.luigeneric.core.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.BgoAdminRoles;

public class AdminRoles implements IProtocolWrite
{
    private int roleBits;

    public AdminRoles(final int roleBits)
    {
        this.roleBits = roleBits;
    }
    public AdminRoles(final BgoAdminRoles bgoAdminRoles)
    {
        this(bgoAdminRoles.value);
    }

    public int getRoleBits()
    {
        return roleBits;
    }

    public void setRoleBits(final int roleBits)
    {
        this.roleBits = roleBits;
    }
    public void setOr(final long other)
    {
        this.setRoleBits(this.roleBits | (int) other);
    }
    public void setAnd(final int other)
    {
        this.setRoleBits(this.roleBits & other);
    }

    public boolean hasOneRole(final BgoAdminRoles... roles)
    {
        for (final BgoAdminRoles role : roles)
        {
            final boolean hasAdminRole = hasRole(role);
            if (hasAdminRole)
                return true;
        }
        return false;
    }
    public boolean hasAllRoles(final BgoAdminRoles... roles)
    {
        for (final BgoAdminRoles role : roles)
        {
            final boolean hasAdminRole = hasRole(role);
            if (!hasAdminRole)
                return false;
        }
        return true;
    }
    public boolean hasRole(final BgoAdminRoles bgoAdminRoles)
    {
        return BgoAdminRoles.hasRole(bgoAdminRoles, this.roleBits);
    }

    public boolean hasPermissions(int requiredPermissions)
    {
        return BgoAdminRoles.hasRole(requiredPermissions, this.roleBits);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(this.roleBits);
    }
}
