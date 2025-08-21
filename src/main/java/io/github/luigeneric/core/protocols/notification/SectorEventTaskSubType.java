package io.github.luigeneric.core.protocols.notification;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum SectorEventTaskSubType implements IProtocolWrite
{
    FREIGHTER(1),

    DRONE_INSURGENT(2);


    private final byte value;

    SectorEventTaskSubType(final int i)
    {
        this.value = (byte) i;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.value);
    }
}
