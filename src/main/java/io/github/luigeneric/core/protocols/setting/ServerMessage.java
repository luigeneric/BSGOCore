package io.github.luigeneric.core.protocols.setting;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Settings(3),
    Keys(4);

    public final int value;
    private static final Map<Integer, ServerMessage> map = new HashMap<>();

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
        return (ServerMessage) map.get(pageType);
    }
}
