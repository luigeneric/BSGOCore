package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.util.List;

public class StarterKitCard extends Card
{
    private final long shipCardGuid;
    private final List<ShipItem> items;


    public StarterKitCard(long cardGuid, long shipCardGuid, List<ShipItem> items)
    {
        super(cardGuid, CardView.StarterKit);
        this.shipCardGuid = shipCardGuid;
        this.items = items;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        //super.write(bw);
        bw.writeGUID(shipCardGuid);
        bw.writeDescArray(items.toArray(new ShipItem[0]));
    }
}
