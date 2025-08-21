package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

public class RemoveModifierUpdate extends PropertyUpdate
{
    private final long toRemoveID;
    public RemoveModifierUpdate(final long modifierID)
    {
        super(SpaceUpdateType.RemoveBuff);
        this.toRemoveID = modifierID;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.toRemoveID);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RemoveModifierUpdate that = (RemoveModifierUpdate) o;

        return toRemoveID == that.toRemoveID;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (toRemoveID ^ (toRemoveID >>> 32));
        return result;
    }
}
