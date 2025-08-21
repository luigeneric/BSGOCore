package io.github.luigeneric.core.spaceentities.statsinfo.buffer.propertyupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.sector.ShipModifier;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.SpaceUpdateType;

import java.util.Objects;

public class AddModifierUpdate extends PropertyUpdate
{
    private final ShipModifier newModifier;
    public AddModifierUpdate(final ShipModifier shipModifier)
    {
        super(SpaceUpdateType.AddBuff);
        this.newModifier = Objects.requireNonNull(shipModifier);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeDesc(this.newModifier);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AddModifierUpdate that = (AddModifierUpdate) o;

        return newModifier.equals(that.newModifier);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + newModifier.hashCode();
        return result;
    }
}
