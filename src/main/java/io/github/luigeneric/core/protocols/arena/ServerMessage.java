package io.github.luigeneric.core.protocols.arena;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Arena1vs1CheckedIn((short) 1),
    Arena3vs3MixedCheckedIn(((short) 1) + 1),
    Arena3vs3MixedRandomCheckedIn(3),
    ArenaDuelCheckedIn(((short) 1) + 3),
    ArenaDuelInviteSlave(((short) 1) + 4),
    ArenaPartyFound(((short) 1) + 5),
    ArenaInvite(((short) 1) + 6),
    ArenaBackToQueue(((short) 1) + 7),
    ArenaFailed(((short) 1) + 8),
    ArenaClosed(((short) 1) + 9),
    ArenaWon(((short) 1) + 10),
    ArenaLost(((short) 1) + 11),
    ArenaInit(((short) 1) + 12),
    ArenaOutOfRange(((short) 1) + 13),
    ArenaOuterRangeOk(((short) 1) + 14),
    ArenaCapturePoint(((short) 1) + 15),
    ArenaOutOfCapturePoint(((short) 1) + 16),
    ArenaRespawn(((short) 1) + 17);

    public static final int SIZE = Short.SIZE;

    public final short shortValue;


    private static final class MappingsHolder
    {
        private static final Map<Short, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(final short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    ServerMessage(final int value)
    {
        this((short) value);
    }


    public static ServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
