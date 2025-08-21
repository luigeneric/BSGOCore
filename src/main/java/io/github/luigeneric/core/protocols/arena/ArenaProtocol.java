package io.github.luigeneric.core.protocols.arena;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.notification.NotificationProtocolWriteOnly;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ArenaProtocol extends BgoProtocol
{
    private final ArenaProtocolWriteOnly writer;

    public ArenaProtocol()
    {
        super(ProtocolID.Arena);
        writer = new ArenaProtocolWriteOnly();
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue((short) msgType);
        switch (clientMessage)
        {
            case Arena1vs1CheckIn, Arena3vs3MixedCheckIn, Arena3vs3MixedRandomCheckIn, ArenaDuelCheckIn ->
            {
                final NotificationProtocolWriteOnly notificationProtocolWriteOnly =
                        ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Notification);
                user.send(notificationProtocolWriteOnly.writeDebugMessage("ARENA NOT IMPLEMENTED"));
                user.send(writer.writeArenaClosed());
            }
            default ->
            {
                log.error("ArenaProtocol could not handle replyType: " + clientMessage);
            }
        }
    }


    enum ClientMessage
    {
        Arena1vs1CheckIn((short)1),
        Arena3vs3MixedCheckIn(((short)1) + 1),
        Arena3vs3MixedRandomCheckIn(((short)1) + 2),
        ArenaDuelCheckIn(((short)1) + 3),
        ArenaCancelCheckIn(((short)1) + 4),
        ArenaInviteOk(((short)1) + 5),
        ArenaInviteCancel(((short)1) + 6),
        ArenaClose(((short)1) + 7);

        public final short shortValue;


        private static final class MappingsHolder
        {
            private static final Map<Short, ClientMessage> mappings = new HashMap<>();
        }

        private static Map<Short, ClientMessage> getMappings()
        {
            return MappingsHolder.mappings;
        }

        ClientMessage(short value)
        {
            shortValue = value;
            getMappings().put(value, this);
        }
        ClientMessage(int i)
        {
            this((short) i);
        }

        public short getValue()
        {
            return shortValue;
        }

        public static ClientMessage forValue(short value)
        {
            return getMappings().get(value);
        }
    }

}
