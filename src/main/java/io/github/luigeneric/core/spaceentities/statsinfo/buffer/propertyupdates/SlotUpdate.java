package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;
import io.github.luigeneric.templates.utils.ObjectStat;

public class SlotUpdate extends PropertyUpdate
{
    private final int slotID;
    private final ObjectStat stat;
    private final float value;

    public SlotUpdate(int slotID, ObjectStat stat, float value)
    {
        super(SpaceUpdateType.SlotStat);
        this.slotID = slotID;
        this.stat = stat;
        this.value = value;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte((byte) this.slotID);
        bw.writeUInt16(this.stat.value);
        bw.writeSingle(this.value);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SlotUpdate that = (SlotUpdate) o;

        if (slotID != that.slotID) return false;
        return stat == that.stat;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + slotID;
        result = 31 * result + (stat != null ? stat.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "SlotUpdate{" +
                "slotID=" + slotID +
                ", stat=" + stat +
                ", value=" + value +
                ", spaceUpdateType=" + spaceUpdateType +
                '}';
    }
}
