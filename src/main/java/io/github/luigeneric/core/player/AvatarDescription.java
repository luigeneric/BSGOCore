package io.github.luigeneric.core.player;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.AvatarItem;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class AvatarDescription extends AvatarItems
{
    public void injectNewAvatarDescription(final Map<AvatarItem, String> map)
    {
        this.items.clear();
        this.items.putAll(map);
    }

    @Override
    public void read(BgoProtocolReader br) throws IOException
    {
        super.read(br);
        final int num = br.readLength(); //weird unused value, should be always 0
        br.readNBytes(num); //the unused num
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeLength(0);
    }

    public Map<AvatarItem, String> getUnmodifiableItems()
    {
        return Collections.unmodifiableMap(this.items);
    }
}

