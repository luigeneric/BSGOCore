package io.github.luigeneric.core.protocols.feedback;

enum UiElementId
{
    ShopWindow,
    RepairWindow,
    ShipShop,
    ShipCustomizationWindow,
    HangarWindow,
    ChangeAmmoMenu,
    InflightShop;

    public static final int SIZE = Short.SIZE;

    public short getValue()
    {
        return (short) this.ordinal();
    }

    public static UiElementId forValue(short value)
    {
        return values()[value];
    }
}
