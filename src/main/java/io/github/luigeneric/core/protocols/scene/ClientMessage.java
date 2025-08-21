package io.github.luigeneric.core.protocols.scene;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    SceneLoaded(1),
    Disconnect(2),
    StopDisconnect(3),
    QuitLogin(4);

    public final int value;
    private static final Map<Integer, ClientMessage> map = new HashMap<>();

    ClientMessage(int value)
    {
        this.value = value;
    }

    static
    {
        for (ClientMessage pageType : ClientMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ClientMessage valueOf(int pageType)
    {
        return map.get(pageType);
    }
}
