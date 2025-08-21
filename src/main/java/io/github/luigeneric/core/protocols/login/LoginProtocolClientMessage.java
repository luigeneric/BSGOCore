package io.github.luigeneric.core.protocols.login;

import java.util.HashMap;
import java.util.Map;

public enum LoginProtocolClientMessage
{
    Init(1),
    Player(2),
    Echo(5);

    public final int value;
    private static final Map<Integer, LoginProtocolClientMessage> map = new HashMap<>();

    LoginProtocolClientMessage(final int value)
    {
        this.value = value;
    }

    static
    {
        for (final LoginProtocolClientMessage pageType : LoginProtocolClientMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static LoginProtocolClientMessage valueOf(int pageType)
    {
        return map.get(pageType);
    }
}
