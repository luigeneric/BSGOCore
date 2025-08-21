package io.github.luigeneric.core.player.container;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.Player;

import java.io.IOException;

public class MoveItemParser
{
    private final BgoProtocolReader br;
    private final Player player;

    private IContainer from;
    private IContainer to;

    private int itemID;
    private long count;
    private boolean equip;

    public MoveItemParser(BgoProtocolReader br, Player player)
    {
        this.br = br;
        this.player = player;
    }

    public void parseMoveItem() throws IOException
    {
        this.from = ContainerFactory.getContainer(br, player);
        if (from.getContainerID().getContainerType() == ContainerType.BlackHole)
            throw new IllegalArgumentException("Item move from BlackHole!");
        this.itemID = br.readUint16();
        this.count = br.readUint32();
        this.to = ContainerFactory.getContainer(br, player);
        this.equip = br.readBoolean();
    }
    public IContainer parseContainer() throws IOException
    {
        return ContainerFactory.getContainer(br, player);
    }
    public void parseMoveAll() throws IOException
    {
        this.from = ContainerFactory.getContainer(br, player);
        this.to = ContainerFactory.getContainer(br, player);
    }
    public void parseUseAugment() throws IOException
    {
        this.from = ContainerFactory.getContainer(br, player);
        this.itemID = br.readUint16();
    }
    public void parseRepairSystem() throws IOException
    {
        this.from = ContainerFactory.getContainer(br, player);
    }

    public long parseAugmentMassActivation() throws IOException
    {
        this.from = ContainerFactory.getContainer(br, player);
        this.itemID = br.readUint16();
        return br.readUint32();
    }



    public IContainer getFrom()
    {
        return from;
    }

    public IContainer getTo()
    {
        return to;
    }

    public int getItemID()
    {
        return itemID;
    }

    public long getCount()
    {
        return count;
    }

    public boolean isEquip()
    {
        return equip;
    }


    @Override
    public String toString()
    {
        return "MoveItemParser{" +
                ", from=" + from +
                ", to=" + to +
                ", itemID=" + itemID +
                ", count=" + count +
                ", equip=" + equip +
                '}';
    }
}
