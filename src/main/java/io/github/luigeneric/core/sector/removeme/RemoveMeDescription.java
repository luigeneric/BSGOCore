package io.github.luigeneric.core.sector.removeme;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.RemovingCause;

public abstract class RemoveMeDescription implements IProtocolWrite
{
    protected final RemovingCause removingCause;

    public RemoveMeDescription(final RemovingCause removingCause)
    {
        this.removingCause = removingCause;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.removingCause.byteValue);
    }
}

