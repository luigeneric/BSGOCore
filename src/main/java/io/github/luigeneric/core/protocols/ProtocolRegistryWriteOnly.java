package io.github.luigeneric.core.protocols;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.community.CommunityProtocolWriteOnly;
import io.github.luigeneric.core.protocols.debug.DebugProtocolWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.protocols.login.LoginProtocolWriteOnly;
import io.github.luigeneric.core.protocols.notification.NotificationProtocolWriteOnly;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.core.protocols.subscribe.SubscribeProtocolWriteOnly;
import io.github.luigeneric.core.protocols.universe.UniverseProtocolWriteOnly;

import java.util.HashMap;
import java.util.Map;

public enum ProtocolRegistryWriteOnly
{
    Login(new LoginProtocolWriteOnly()),
    Notification(new NotificationProtocolWriteOnly()),
    Player(new PlayerProtocolWriteOnly()),
    Subscribe(new SubscribeProtocolWriteOnly()),
    Community(new CommunityProtocolWriteOnly()),
    Universe(new UniverseProtocolWriteOnly()),
    Debug(new DebugProtocolWriteOnly()),

    Game(new GameProtocolWriteOnly());


    private static final Map<ProtocolID, WriteOnlyProtocol> writeOnlyProtocolMap = new HashMap<>();
    private final WriteOnlyProtocol writeOnlyProtocol;
    ProtocolRegistryWriteOnly(final WriteOnlyProtocol writeOnlyProtocol)
    {
        this.writeOnlyProtocol = writeOnlyProtocol;
    }

    static
    {
        for (final ProtocolRegistryWriteOnly value : ProtocolRegistryWriteOnly.values())
        {
            writeOnlyProtocolMap.put(value.writeOnlyProtocol.getProtocolID(), value.writeOnlyProtocol);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends WriteOnlyProtocol> T getProtocol(final ProtocolID protocolID)
    {
        return (T) writeOnlyProtocolMap.get(protocolID);
    }

    /**
     * Short because it's almost the only one used so far
     */
    public static GameProtocolWriteOnly game()
    {
        return (GameProtocolWriteOnly) Game.writeOnlyProtocol;
    }

    public static BgoProtocolWriter writeDebugMessage(final String message)
    {
        final DebugProtocolWriteOnly debugProtocolWriteOnly = getProtocol(ProtocolID.Debug);
        return debugProtocolWriteOnly.writeMessage(message);
    }
}
