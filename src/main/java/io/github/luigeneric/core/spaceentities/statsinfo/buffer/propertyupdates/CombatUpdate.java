package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

public class CombatUpdate extends PropertyUpdate
{
    private final boolean isInCombat;
    public CombatUpdate(final boolean isInCombat)
    {
        super(SpaceUpdateType.CombatStatus);
        this.isInCombat = isInCombat;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeBoolean(this.isInCombat);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CombatUpdate that = (CombatUpdate) o;

        return isInCombat == that.isInCombat;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (isInCombat ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "CombatUpdate{" +
                "isInCombat=" + isInCombat +
                ", spaceUpdateType=" + spaceUpdateType +
                '}';
    }
}
