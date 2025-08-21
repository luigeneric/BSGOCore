package io.github.luigeneric.core.protocols.zone;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage implements IProtocolWrite
{
    ActiveZones(1),
    UpcomingZones(2),
    ScoreKillspam(3),
    ScoreNemesisUpdate(4),
    ScoreSpreeUpdate(5),
    ScoreLeaderUpdate(6),
    ScoreboardUpdate(7),
    @Deprecated(since = "only debug log")
    FactionBoardUpdate(8),
    @Deprecated(since = "only debug+combat log")
    FactionBoardTickets(9),
    AdmissionStatus(10);

    public static final int SIZE = Short.SIZE;

    public final short value;

    ServerMessage(final int i)
    {
        this((short) i);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeMsgType(this.value);
    }

    private static final class MappingsHolder
    {
        private static final Map<Short, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(short value)
    {
        this.value = value;
        getMappings().put(value, this);
    }

    public static ServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
