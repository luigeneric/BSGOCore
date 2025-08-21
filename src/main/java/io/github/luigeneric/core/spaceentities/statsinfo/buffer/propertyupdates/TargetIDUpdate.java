package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

public class TargetIDUpdate extends PropertyUpdate
{
    private final long targetID;

    public TargetIDUpdate(final long targetID)
    {
        super(SpaceUpdateType.TargetID);
        this.targetID = targetID;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.targetID);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TargetIDUpdate that = (TargetIDUpdate) o;

        return targetID == that.targetID;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (targetID ^ (targetID >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "TargetIDUpdate{" +
                "targetID=" + targetID +
                ", spaceUpdateType=" + spaceUpdateType +
                '}';
    }
}
