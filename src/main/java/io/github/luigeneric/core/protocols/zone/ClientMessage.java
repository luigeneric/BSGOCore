package io.github.luigeneric.core.protocols.zone;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    Join(1),
    Leave(2),
    ScoreboardSubscribe(3),
    ScoreboardUnsubscribe(4),
    UseFtlOverride(5),
    AdmissionStatus(6);

    public final int value;
    private static final Map<Integer, ClientMessage> map = new HashMap<>();

    ClientMessage(int value) {
        this.value = value;
    }

    static {
        for (final ClientMessage pageType : ClientMessage.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static ClientMessage valueOf(int pageType) {
        return map.get(pageType);
    }

    public int getValue() {
        return value;
    }
}
