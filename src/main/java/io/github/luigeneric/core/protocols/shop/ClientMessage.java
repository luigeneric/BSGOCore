package io.github.luigeneric.core.protocols.shop;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    Items(1),
    Close(11),
    BoughtShipSaleOffer(13),
    EventShopItems(15),
    AllSales(17);

    public final short shortValue;

    private static final class MappingsHolder
    {
        private static final Map<Short, ClientMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ClientMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ClientMessage(final int value)
    {
        shortValue = (short) value;
        getMappings().put(shortValue, this);
    }

    public static ClientMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
