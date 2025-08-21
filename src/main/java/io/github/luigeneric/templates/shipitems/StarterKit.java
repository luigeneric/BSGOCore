package io.github.luigeneric.templates.shipitems;

public class StarterKit extends ShipItem
{

    public StarterKit(long cardGuid, int serverID)
    {
        super(cardGuid, ItemType.Starter, serverID);
    }

    @Override
    public ShipItem copy()
    {
        return new StarterKit(cardGuid, this.getServerID());
    }
}
