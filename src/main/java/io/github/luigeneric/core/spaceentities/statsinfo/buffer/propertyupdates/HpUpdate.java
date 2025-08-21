package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

public class HpUpdate extends PropertyUpdate
{
    private final float hp;
    public HpUpdate(final float hp)
    {
        super(SpaceUpdateType.HullPoints);
        this.hp = hp;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeSingle(this.hp);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HpUpdate hpUpdate = (HpUpdate) o;

        return Float.compare(hpUpdate.hp, hp) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (hp != 0.0f ? Float.floatToIntBits(hp) : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "HpUpdate{" +
                "hp=" + hp +
                ", spaceUpdateType=" + spaceUpdateType +
                '}';
    }
}
