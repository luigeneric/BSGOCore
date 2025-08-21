package io.github.luigeneric.core.protocols.community;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    PartyInvitePlayer((short) 1),
    PartyDismissPlayer(((short) 1) + 1),
    PartyLeave(((short) 1) + 2),
    PartyAppointLeader(((short) 1) + 3),
    PartyAccept(((short) 1) + 4),
    FriendInvite(((short) 1) + 5),
    FriendAccept(((short) 1) + 6),
    FriendRemove(((short) 1) + 7),
    IgnoreAdd(((short) 1) + 8),
    IgnoreRemove(((short) 1) + 9),
    ChatConnected(((short) 1) + 10),
    ChatFleetAllowed(((short) 1) + 11),
    ChatAuthFailed(((short) 1) + 12),
    GuildStart(((short) 1) + 13),
    GuildLeave(((short) 1) + 14),
    GuildChangeRankName(((short) 1) + 15),
    GuildChangeRankPermissions(((short) 1) + 16),
    GuildPromote(((short) 1) + 17),
    GuildKick(((short) 1) + 18),
    GuildInvite((short) 21),
    GuildAccept(((short) 21) + 1),
    RecruitInvited((short) 24),
    PartyChatInvite(32),
    RecruitLevel(33),
    PartyMemberFtlState(34),
    IgnoreClear(35);

    public static final int SIZE = Short.SIZE;

    public final short shortValue;


    private static final class MappingsHolder
    {
        private static final Map<Short, ClientMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ClientMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ClientMessage(final short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    ClientMessage(final int i)
    {
        this((short) i);
    }

    public static ClientMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
