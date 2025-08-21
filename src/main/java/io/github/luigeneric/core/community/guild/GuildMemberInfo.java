package io.github.luigeneric.core.community.guild;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.enums.GuildRole;

import java.time.LocalDateTime;

public class GuildMemberInfo implements IProtocolWrite
{
    private final long playerID;
    private final String playerName;
    private final short playerLevel;
    private GuildRole guildRole;
    private final BgoTimeStamp lastLogout;
    private final Location playerLocation;

    public GuildMemberInfo(long playerID, String playerName, short playerLevel,
                           GuildRole guildRole, LocalDateTime lastLogout, Location playerLocation)
    {
        this.playerID = playerID;
        this.playerName = playerName;
        this.playerLevel = playerLevel;
        this.guildRole = guildRole;
        this.lastLogout = new BgoTimeStamp(lastLogout);
        this.playerLocation = playerLocation;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(playerID);
        bw.writeString(playerName);
        bw.writeByte((byte) playerLevel);
        bw.writeDesc(guildRole);
        bw.writeLongDateTime(this.lastLogout.getLocalDate());
        playerLocation.processGuildLocation(bw);
    }

    public long getPlayerID()
    {
        return playerID;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerRole(final GuildRole playerRole)
    {
        this.guildRole = playerRole;
    }

    public short getPlayerLevel()
    {
        return playerLevel;
    }

    public GuildRole getPlayerRole()
    {
        return guildRole;
    }

    public BgoTimeStamp getLastLogout()
    {
        return lastLogout;
    }
}
