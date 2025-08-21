package io.github.luigeneric.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum BgoAdminRoles
{
    None(0),
    View(1),
    Edit(2),
    Ban(4),
    CommunityManager(8),
    Developer(16),
    Console(32),
    GodMode(1024),
    Mod(2048);

    public static final int SIZE = Integer.SIZE;

    public final int value;

    public static int ofRoles(final BgoAdminRoles... roles)
    {
        int num = 0;
        for (final BgoAdminRoles role : roles)
        {
            num |= role.value;
        }
        return num;
    }

    private static final class MappingsHolder
    {
        private static final Map<Integer, BgoAdminRoles> mappings = new HashMap<>();
    }

    private static Map<Integer, BgoAdminRoles> getMappings()
    {
        return MappingsHolder.mappings;
    }

    BgoAdminRoles(final int value)
    {
        this.value = value;
        getMappings().put(value, this);
    }

    public static BgoAdminRoles forValue(int value)
    {
        return getMappings().get(value);
    }

    public static List<BgoAdminRoles> getRoles(final int roleBits)
    {
        List<BgoAdminRoles> roles = new ArrayList<>();
        for (final BgoAdminRoles value : values())
        {
            final int res = value.value & roleBits;
            if (res > 0)
                roles.add(value);
        }
        return roles;
    }
    public static boolean hasRole(final BgoAdminRoles role, final int permissions)
    {
        if (role == None)
            return true;
        final int tmp = (role.value & permissions);
        return tmp == role.value;
    }
    public static boolean hasRole(final int roles, final int permissions)
    {
        return (roles & permissions) == roles;
    }
}
