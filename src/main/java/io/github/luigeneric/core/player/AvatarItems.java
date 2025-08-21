package io.github.luigeneric.core.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolRead;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.AvatarItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AvatarItems implements IProtocolRead, IProtocolWrite
{
    protected final Map<AvatarItem, String> items;

    public AvatarItems()
    {
        this.items = new HashMap<>();
    }

    @Override
    public void read(final BgoProtocolReader br) throws IOException
    {
        final int count = br.readLength();
        for (int i = 0; i < count; i++)
        {
            final AvatarItem avatarItem = AvatarItem.forValue(br.readByte());
            this.items.put(avatarItem, br.readString());
        }
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeLength(this.items.size());
        for (Map.Entry<AvatarItem, String> entry : items.entrySet())
        {
            bw.writeByte(entry.getKey().getValue());
            bw.writeString(entry.getValue());
        }
    }
}
