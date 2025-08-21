package io.github.luigeneric.core.sector.removeme;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.RemovingCause;

public class DeathRemoveMeDescription extends RemoveMeDescription
{
    protected final long killedByObjectId;
    public DeathRemoveMeDescription(final long killedByObjectId)
    {
        super(RemovingCause.Death);
        this.killedByObjectId = killedByObjectId;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.killedByObjectId);
    }
}
