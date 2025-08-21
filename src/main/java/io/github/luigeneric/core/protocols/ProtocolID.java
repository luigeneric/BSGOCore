package io.github.luigeneric.core.protocols;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ProtocolID
{
    Login(0),
    Universe(1),
    Game(2),
    Sync(3),
    Player(4),
    Debug(5),
    Catalogue(6),
    Ranking(7),
    Story(8),
    Scene(9),
    Room(10),
    Community(11),
    Shop(12),
    Setting(13),
    Ship(14),
    Dialog(15),
    Market(16),
    Notification(17),
    Subscribe(18),
    Feedback(19),
    @Deprecated // Tournament functionality is now implemented as part of ZoneProtocol
    Tournament(20),
    Arena(21),
    Battlespace(22),
    Wof(23),
    Zone(24);


    public final byte value;

    ProtocolID(final int b)
    {
        this((byte) b);
    }
    ProtocolID(final byte b)
    {
        this.value = b;
        getMappings().put(b, this);
    }

    @Deprecated
    public static Optional<ProtocolID> valueOf(int value)
    {
        return Arrays.stream(values())
                .filter(protocolID1 -> protocolID1.value == value)
                .findFirst();
    }

    public static ProtocolID forValue(final int value)
    {
        return getMappings().get((byte) value);
    }


    private static final class MappingsHolder
    {
        private static final Map<Byte, ProtocolID> mappings = new HashMap<>();
    }

    private static Map<Byte, ProtocolID> getMappings()
    {
        return MappingsHolder.mappings;
    }
}
