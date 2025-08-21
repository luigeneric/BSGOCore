package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

public class PpUpdate extends PropertyUpdate
{
    private final float pp;

    public PpUpdate(final float pp)
    {
        super(SpaceUpdateType.PowerPoints);
        this.pp = pp;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeSingle(this.pp);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PpUpdate ppUpdate = (PpUpdate) o;

        return Float.compare(ppUpdate.pp, pp) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (pp != 0.0f ? Float.floatToIntBits(pp) : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "PpUpdate{" +
                "pp=" + pp +
                ", spaceUpdateType=" + spaceUpdateType +
                '}';
    }
}
