package io.github.luigeneric.core.protocols.scene;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    LoadNextScene(1),
    DisconnectTimer(2),
    Disconnect(100);

    public final int value;
    private static Map<Integer, ServerMessage> map = new HashMap<>();

    ServerMessage(int value)
    {
        this.value = value;
    }

    static
    {
        for (ServerMessage pageType : ServerMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ServerMessage valueOf(int pageType)
    {
        return map.get(pageType);
    }
}
