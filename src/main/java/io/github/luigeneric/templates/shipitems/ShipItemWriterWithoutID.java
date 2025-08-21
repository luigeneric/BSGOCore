package io.github.luigeneric.templates.shipitems;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.List;

public class ShipItemWriterWithoutID
{
    private ShipItemWriterWithoutID(){}

    public static void write(final BgoProtocolWriter bw, final ShipItem shipItem)
    {
        //bw.writeUInt16(shipItem.getServerID()); now without this
        bw.writeByte(shipItem.itemType.getValue());
        if (shipItem.itemType!= ItemType.None)
        {
            bw.writeUInt32(shipItem.getCardGuid());
        }
    }
    public static void write(final BgoProtocolWriter bw, final ItemCountable shipItem)
    {
        write(bw, (ShipItem) shipItem);
        bw.writeUInt32(shipItem.getCount());
    }
    public static void write(final BgoProtocolWriter bw, final ShipSystem shipSystem)
    {
        write(bw, (ShipItem) shipSystem);
        bw.writeSingle(shipSystem.getDurability());
        bw.writeDouble(shipSystem.getTimeOfLastUse());
    }


    public static void write(final BgoProtocolWriter bw, final List<ShipItem> itemList)
    {
        bw.writeLength(itemList.size());
        for (ShipItem item : itemList)
        {
            if (item instanceof ItemCountable itemCountable)
            {
                write(bw, itemCountable);
            }
            else if (item instanceof ShipSystem sy)
            {
                write(bw, sy);
            }
            else
            {
                write(bw, item);
            }

        }
    }

    public static void writeNone(BgoProtocolWriter bw)
    {
        bw.writeByte(ItemType.None.getValue());
    }
}
