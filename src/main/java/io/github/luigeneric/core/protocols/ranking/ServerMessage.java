package io.github.luigeneric.core.protocols.ranking;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    @Deprecated //old highscore system
    ReplyRankingTab( 4),
    @Deprecated //old highscore system
    ReplyPlayerRank(6),

    /**
     * RankDescription
     */
    ReplyRankingCounter( 8),

    @Deprecated //not used anymore
    ReplyRankingCounterPlayer( 10),
    ReplyRankingTournament( 12),
    ReplyRankingTournamentPlayer( 14);

    public final short shortValue;

    private static final class MappingsHolder
    {
        private static final Map<Short, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(int value)
    {
        this((short) value);
    }
    ServerMessage(short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    public short getValue()
    {
        return shortValue;
    }

    public static ServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
