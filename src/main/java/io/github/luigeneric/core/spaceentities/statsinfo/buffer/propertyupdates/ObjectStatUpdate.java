package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;
import io.github.luigeneric.templates.utils.ObjectStat;

public class ObjectStatUpdate extends PropertyUpdate
{
    private final ObjectStat stat;
    private final float value;

    public ObjectStatUpdate(ObjectStat stat, float value)
    {
        super(SpaceUpdateType.ObjectStat);
        this.stat = stat;
        this.value = value;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt16(this.stat.value);
        bw.writeSingle(this.value);
    }

    @Override
    public String toString()
    {
        return "ObjectStatUpdate{" +
                "stat=" + stat +
                ", value=" + value +
                ", spaceUpdateType=" + spaceUpdateType +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ObjectStatUpdate that = (ObjectStatUpdate) o;

        if (Float.compare(that.value, value) != 0) return false;
        return stat == that.stat;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (stat != null ? stat.hashCode() : 0);
        result = 31 * result + (value != 0.0f ? Float.floatToIntBits(value) : 0);
        return result;
    }

    public ObjectStat getStat()
    {
        return stat;
    }

    public float getValue()
    {
        return value;
    }
}
