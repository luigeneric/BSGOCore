package io.github.luigeneric.core.protocols.shop;

import java.util.HashMap;
import java.util.Map;

public enum ServerMessage
{
    Items((short) 2),
    Sales(((short) 2) + 1),
    UpgradeSales(((short) 2) + 2),
    ShopPrices((short) 8),
    BoughtShipSaleOffer((short) 12),
    EventShopItems((short) 14),
    EventShopAvailable((short) 16);

    public static final int SIZE = Short.SIZE;

    public final short shortValue;
    private static HashMap<Short, ServerMessage> mappings;

    ServerMessage(int i)
    {
        this((short) i);
    }

    private static Map<Short, ServerMessage> getMappings()
    {
        if (mappings == null)
        {
            synchronized (ServerMessage.class)
            {
                if (mappings == null)
                {
                    mappings = new HashMap<>();
                }
            }
        }
        return mappings;
    }

    ServerMessage(short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    public static ServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
