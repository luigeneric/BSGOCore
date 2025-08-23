package io.github.luigeneric.templates.startupconfig;

import io.smallrye.config.ConfigMapping;

import java.util.Set;

@ConfigMapping(prefix = "gameserver")
public interface GameServerParamsConfig
{
    int port();
    int loginServerPort();
    int chatServerPort();
    int maxBacklog();
    int maxClients();
    String chatServerAddress();

    boolean ignoreHashes();
    Set<String> allowedHashes();

    StarterParams starterParams();

    FactionChangeParams factionChangeParams();

    PermissionParams permissionParams();

    SessionSettings sessionSettings();

    default boolean shouldConnectToChatServer()
    {
        return chatServerPort() != 0;
    }


    interface FactionChangeParams
    {
        long cooldownFactionSwitch();
        float cubitsPriceFaction();
    }

    interface PermissionParams
    {
        Set<Long> devAccountIds();
    }

    interface SessionSettings
    {
        boolean ignoreSession();

        Set<String> sessions();
    }
}

