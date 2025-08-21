package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

public abstract class PropertyUpdate implements IProtocolWrite
{
    protected final SpaceUpdateType spaceUpdateType;

    public PropertyUpdate(SpaceUpdateType spaceUpdateType)
    {
        this.spaceUpdateType = spaceUpdateType;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.spaceUpdateType.getValue());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyUpdate that = (PropertyUpdate) o;

        return spaceUpdateType == that.spaceUpdateType;
    }

    @Override
    public int hashCode()
    {
        return spaceUpdateType != null ? spaceUpdateType.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "PropertyUpdate{" +
                "spaceUpdateType=" + spaceUpdateType +
                '}';
    }

    public SpaceUpdateType getSpaceUpdateType()
    {
        return spaceUpdateType;
    }
}