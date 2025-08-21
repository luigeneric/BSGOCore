package io.github.luigeneric.core.protocols.login;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Hello(0),
    Init(1),
    Error(2),
    Player(3),
    Wait(4),
    Echo(5);

    public final int value;
    private static final Map<Integer, ServerMessage> map = new HashMap<>();

    ServerMessage(int value)
    {
        this.value = value;
    }

    static
    {
        for (final ServerMessage pageType : ServerMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ServerMessage valueOf(final int pageType)
    {
        return map.get(pageType);
    }
}
