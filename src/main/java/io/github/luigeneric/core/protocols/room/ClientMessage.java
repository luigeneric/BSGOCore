package io.github.luigeneric.core.protocols.room;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    Talk(0),
    NpcMarks(2),
    EnterDoor(4),
    Quit(5),
    Enter(6);

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

    public int getValue()
    {
        return value;
    }
}
