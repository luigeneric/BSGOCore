package io.github.luigeneric.core.spaceentities.bindings;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.ShipAspect;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ShipAspects implements IProtocolWrite
{
    private final Set<ShipAspect> shipAspects;

    public ShipAspects(final Set<ShipAspect> shipAspects)
    {
        Objects.requireNonNull(shipAspects, "shipAspects cannot be null!");
        this.shipAspects = shipAspects;
    }
    public ShipAspects(final ShipAspect... shipAspects)
    {
        this(new HashSet<>(Set.of(shipAspects)));
    }

    public void addAspect(final ShipAspect aspect)
    {
        this.shipAspects.add(aspect);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        int shipAspectSize = shipAspects.size();
        bw.writeLength(shipAspectSize);
        for (ShipAspect shipAspect : shipAspects)
        {
            bw.write(shipAspect.value);
        }
    }

    @Override
    public String toString()
    {
        return "ShipAspects{" +
                "shipAspects=" + shipAspects +
                '}';
    }


}
