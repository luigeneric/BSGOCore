package io.github.luigeneric.core.protocols.community;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Party(1),
    PartyIgnore(2),
    PartyInvite(3),
    FriendInvite(4),
    FriendAccept(5),
    FriendRemove(6),
    FriendAdd(7),
    IgnoreAdd(8),
    IgnoreRemove(9),
    ChatSessionId(10),
    ChatFleetAllowed(11),
    GuildQuit(12),
    GuildRemove(13),
    GuildInvite(14),
    GuildInfo(15),
    GuildSetPromotion(16),
    GuildMemberUpdate(17),
    GuildSetChangeRankName(18),
    GuildSetChangePermissions(19),
    GuildStartError(21),
    GuildJoinError(22),
    GuildInviteResult(23),
    GuildOperationResult(24),
    Recruits(26),
    ActivateJumpTargetTransponder(37),
    CancelJumpTargetTransponder(38),
    PartyAnchor(39),
    PartyChatInviteFailed(40),
    RecruitLevel(41),
    PartyMemberFtlState(42);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Integer, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public static ServerMessage forValue(int value)
    {
        return getMappings().get(value);
    }
}
