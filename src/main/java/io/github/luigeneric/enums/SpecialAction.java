package io.github.luigeneric.enums;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum SpecialAction implements IProtocolWrite
{
    None(0),
    Assist(0.5),
    Killer(0.7),
    Saviour(0),
    Avenger(0),
    Buffer(0.15),
    Debuffer(0.15),
    AssistCountingAsKill(1),
    Tank(0);

    public static final int SIZE = Short.SIZE;

    public final float lootMultiplier;

    SpecialAction(final double lootMultiplier)
    {
        this.lootMultiplier = (float) lootMultiplier;
    }

    public short getValue()
    {
        return (short) this.ordinal();
    }

    public static SpecialAction forValue(short value)
    {
        return values()[value];
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.getValue());
    }
}
