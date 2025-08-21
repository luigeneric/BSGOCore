package io.github.luigeneric.templates.shipitems;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.List;

public class ItemProvider
{
    public static void writeItem(final BgoProtocolWriter bw, final ShipItem shipItem)
    {
        bw.writeByte(shipItem.itemType.getValue());
        bw.writeDesc(shipItem);
    }

    public static void writeItems(final BgoProtocolWriter bw, final List<ShipItem> shipItemList)
    {
        bw.writeUInt16(shipItemList.size());
        for(final ShipItem item : shipItemList)
        {
            writeItem(bw, item);
        }
    }
}
