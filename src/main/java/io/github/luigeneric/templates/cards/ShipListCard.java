package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.Arrays;

public class ShipListCard extends Card
{
    private final long[] shipCardGuids;
    private final long[] upgradeShipCardGuids;

    public ShipListCard(long cardGuid, long[] shipCardGuids, long[] upgradeShipCardGuids)
    {
        super(cardGuid, CardView.ShipList);
        this.shipCardGuids = shipCardGuids;
        this.upgradeShipCardGuids = upgradeShipCardGuids;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        final int len = shipCardGuids.length;
        bw.writeLength(len);
        for (int i = 0; i < len; i++)
        {
            bw.writeGUID(shipCardGuids[i]);
            bw.writeGUID(upgradeShipCardGuids[i]);
        }
    }

    @Override
    public String toString()
    {
        return "ShipListCard{" +
                "shipCardGuids=" + Arrays.toString(shipCardGuids) +
                ", upgradeShipCardGuids=" + Arrays.toString(upgradeShipCardGuids) +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public long[] getShipCardGuids()
    {
        return shipCardGuids;
    }

    public long[] getUpgradeShipCardGuids()
    {
        return upgradeShipCardGuids;
    }
}
