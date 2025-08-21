package io.github.luigeneric.templates.shipitems;

public class StoreShip extends ShipItem
{
    public StoreShip(long cardGuid, int serverID)
    {
        super(cardGuid, ItemType.Ship, serverID);
    }

    @Override
    public ShipItem copy()
    {
        return new StoreShip(cardGuid, this.getServerID());
    }
}
