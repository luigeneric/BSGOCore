package io.github.luigeneric.core.player.container.containerids;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.IContainerID;

import java.io.IOException;

public class ShipSlotContainerID extends IContainerID
{
    private int shipID;
    private int slotID;


    /**
     * Creates a new slotcontainer, first shipid then slotid
     * @param shipID the serverID of the ship
     * @param slotID the serverID of the slot
     */
    public ShipSlotContainerID(final int shipID, final int slotID)
    {
        super(ContainerType.ShipSlot);
        this.shipID = shipID;
        this.slotID = slotID;
    }

    public ShipSlotContainerID()
    {
        this(0, 0);
    }


    @Override
    public void read(final BgoProtocolReader br) throws IOException
    {
        this.shipID = br.readUint16();
        this.slotID = br.readUint16();
    }

    public int getShipID()
    {
        return shipID;
    }

    public int getSlotID()
    {
        return slotID;
    }

    @Override
    public String toString()
    {
        return "ShipSlotContainerID{" +
                "shipID=" + shipID +
                ", slotID=" + slotID +
                '}';
    }
}
